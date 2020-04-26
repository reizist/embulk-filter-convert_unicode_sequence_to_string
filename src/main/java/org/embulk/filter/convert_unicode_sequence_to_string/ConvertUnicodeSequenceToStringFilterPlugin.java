package org.embulk.filter.convert_unicode_sequence_to_string;

import org.embulk.config.Config;
import org.embulk.config.ConfigSource;
import org.embulk.config.Task;
import org.embulk.config.TaskSource;
import org.embulk.spi.Exec;
import org.embulk.spi.FilterPlugin;
import org.embulk.spi.Page;
import org.embulk.spi.PageBuilder;
import org.embulk.spi.PageOutput;
import org.embulk.spi.PageReader;
import org.embulk.spi.Schema;

import java.util.List;

public class ConvertUnicodeSequenceToStringFilterPlugin
        implements FilterPlugin
{
    public interface PluginTask
            extends Task
    {
        @Config("target_columns")
        List<String> getTargetColumns();
    }

    @Override
    public void transaction(ConfigSource config, Schema inputSchema,
            FilterPlugin.Control control)
    {
        PluginTask task = config.loadConfig(PluginTask.class);

        Schema outputSchema = inputSchema;

        control.run(task.dump(), outputSchema);
    }

    @Override
    public PageOutput open(TaskSource taskSource, Schema inputSchema,
            Schema outputSchema, PageOutput output)
    {
        PluginTask task = taskSource.loadTask(PluginTask.class);
        PageReader pageReader = new PageReader(inputSchema);
        PageBuilder pageBuilder = new PageBuilder(Exec.getBufferAllocator(), outputSchema, output);
        ColumnVisitorImpl visitor = new ColumnVisitorImpl(task, pageReader, pageBuilder);

        return new PageOutputImpl(pageReader, pageBuilder, outputSchema, visitor);
    }

    public static class PageOutputImpl implements PageOutput
    {
        private PageReader pageReader;
        private PageBuilder pageBuilder;
        private Schema outputSchema;
        private ColumnVisitorImpl visitor;

        PageOutputImpl(PageReader pageReader, PageBuilder pageBuilder, Schema outputSchema, ColumnVisitorImpl visitor)
        {
            this.pageReader = pageReader;
            this.pageBuilder = pageBuilder;
            this.outputSchema = outputSchema;
            this.visitor = visitor;
        }

        @Override
        public void finish()
        {
            pageBuilder.finish();
        }

        @Override
        public void close()
        {
            pageBuilder.close();
        }

        @Override
        public void add(Page page)
        {
            pageReader.setPage(page);

            while (pageReader.nextRecord()) {
                outputSchema.visitColumns(visitor);
                pageBuilder.addRecord();
            }
        }
    };
}

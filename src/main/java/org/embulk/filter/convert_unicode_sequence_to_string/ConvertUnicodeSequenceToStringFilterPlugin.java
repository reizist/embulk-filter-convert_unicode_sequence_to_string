package org.embulk.filter.convert_unicode_sequence_to_string;

import org.embulk.config.Config;
import org.embulk.config.ConfigDefault;
import org.embulk.config.ConfigException;
import org.embulk.config.ConfigSource;
import org.embulk.config.Task;
import org.embulk.config.TaskSource;
import org.embulk.spi.Column;
import org.embulk.spi.FilterPlugin;
import org.embulk.spi.PageOutput;
import org.embulk.spi.Schema;
import org.embulk.spi.Page;
import org.embulk.spi.PageReader;
import org.embulk.spi.PageBuilder;
import org.embulk.spi.Exec;
import org.embulk.spi.type.Types;
import org.embulk.spi.ColumnVisitor;

import java.util.List;

public class ConvertUnicodeSequenceToStringFilterPlugin
        implements FilterPlugin
{
    public interface PluginTask
            extends Task
    {
        // configuration option 1 (required integer)
        @Config("target_columns")
        @ConfigDefault("[]")
        public List<String> getTargetColumns();
    }

    public class StringConverterVisitorImpl implements ColumnVisitor {
        private final PageReader pageReader;
        private final PageBuilder pageBuilder;

        StringConverterVisitorImpl(PageReader pageReader, PageBuilder pageBuilder)
        {
            this.pageReader = pageReader;
            this.pageBuilder = pageBuilder;
        }

        public boolean visitColumns(Schema inputSchema) {
            return false;
        }

        @Override
        public void booleanColumn(Column column)
        {
            if (pageReader.isNull(column)) {
                pageBuilder.setNull(column);
            }
            else {
                pageBuilder.setBoolean(column, pageReader.getBoolean(column));
            }
        }

        @Override
        public void longColumn(Column column)
        {
            if (pageReader.isNull(column)) {
                pageBuilder.setNull(column);
            }
            else {
                pageBuilder.setLong(column, pageReader.getLong(column));
            }
        }

        @Override
        public void doubleColumn(Column column)
        {
            if (pageReader.isNull(column)) {
                pageBuilder.setNull(column);
            }
            else {
                pageBuilder.setDouble(column, pageReader.getDouble(column));
            }
        }

        @Override
        public void stringColumn(Column column)
        {
            if (pageReader.isNull(column)) {
                pageBuilder.setNull(column);
            }
            else {
                pageBuilder.setString(column, "hoge");
            }
        }

        @Override
        public void timestampColumn(Column column)
        {
            if (pageReader.isNull(column)) {
                pageBuilder.setNull(column);
            }
            else {
                pageBuilder.setTimestamp(column, pageReader.getTimestamp(column));
            }
        }

        @Override
        public void jsonColumn(Column column)
        {
            if (pageReader.isNull(column)) {
                pageBuilder.setNull(column);
            }
            else {
                pageBuilder.setJson(column, pageReader.getJson(column));
            }
        }
    }

    @Override
    public void transaction(ConfigSource config, Schema inputSchema,
            FilterPlugin.Control control)
    {
        PluginTask task = config.loadConfig(PluginTask.class);

        configure(task);

        Schema outputSchema = inputSchema;

        control.run(task.dump(), outputSchema);
    }

    private void configure(PluginTask task)
    {
        if (task.getTargetColumns().size() < 1) {
            throw new ConfigException("\"target_columns\" can be specified.");
        }
    }

    @Override
    public PageOutput open(TaskSource taskSource, Schema inputSchema,
            Schema outputSchema, PageOutput output)
    {
        PluginTask task = taskSource.loadTask(PluginTask.class);
        PageReader pageReader = new PageReader(inputSchema);
        PageBuilder pageBuilder = new PageBuilder(Exec.getBufferAllocator(), outputSchema, output);
        StringConverterVisitorImpl visitor = new StringConverterVisitorImpl(pageReader, pageBuilder);

        return new PageOutputImpl(task, pageReader, pageBuilder, outputSchema, visitor, inputSchema);
    }

    public static class PageOutputImpl implements PageOutput
    {
        private PluginTask task;
        private PageReader pageReader;
        private PageBuilder pageBuilder;
        private Schema outputSchema;
        private Schema inputSchema;
        private StringConverterVisitorImpl visitor;

        PageOutputImpl(PluginTask task, PageReader pageReader, PageBuilder pageBuilder, Schema outputSchema, StringConverterVisitorImpl visitor, Schema inputSchema)
        {
            this.task = task;
            this.pageReader = pageReader;
            this.pageBuilder = pageBuilder;
            this.outputSchema = outputSchema;
            this.visitor = visitor;
            this.inputSchema = inputSchema;
        }


        @Override
        public void finish() {
            pageBuilder.finish();
        }

        @Override
        public void close() {
            pageBuilder.close();
        }

        @Override
        public void add(Page page) {
            pageReader.setPage(page);

            while (pageReader.nextRecord()) {
                for (Column column: inputSchema.getColumns()) {
                    if (Types.STRING.equals(column.getType())) {
                        for (String targetColumnName: task.getTargetColumns()) {
                            if (column.getName().equals(targetColumnName)) {
                                outputSchema.visitColumns(visitor);
                            }
                        }
                    }
                }
                pageBuilder.addRecord();
            }
        }
    };
}

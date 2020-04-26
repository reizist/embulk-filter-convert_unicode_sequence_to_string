package org.embulk.filter.convert_unicode_sequence_to_string;

import org.embulk.EmbulkTestRuntime;
import org.embulk.config.ConfigException;
import org.embulk.config.ConfigLoader;
import org.embulk.config.ConfigSource;
import org.embulk.config.TaskSource;
import org.embulk.filter.convert_unicode_sequence_to_string.ConvertUnicodeSequenceToStringFilterPlugin.PluginTask;
import org.embulk.spi.Exec;
import org.embulk.spi.Page;
import org.embulk.spi.PageOutput;
import org.embulk.spi.PageReader;
import org.embulk.spi.PageTestUtils;
import org.embulk.spi.Schema;
import org.embulk.spi.TestPageBuilderReader.MockPageOutput;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.embulk.filter.convert_unicode_sequence_to_string.ConvertUnicodeSequenceToStringFilterPlugin.Control;
import static org.embulk.spi.type.Types.STRING;
import static org.junit.Assert.assertEquals;

public class TestConvertUnicodeSequenceToStringFilterPlugin
{
    @Rule public EmbulkTestRuntime runtime = new EmbulkTestRuntime();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private ConfigSource getConfigFromYaml(String yaml)
    {
        ConfigLoader loader = new ConfigLoader(Exec.getModelManager());
        return loader.fromYamlString(yaml);
    }

    @Test
    public void testThrowExceptionAbsentTargetColumns()
    {
        String configYaml = "" +
                "type: convert_unicode_sequence_to_string\n";
        ConfigSource config = getConfigFromYaml(configYaml);

        exception.expect(ConfigException.class);
        exception.expectMessage("Field 'target_columns' is required but not set");
        config.loadConfig(PluginTask.class);
    }

    @Test
    public void testThrowExceptionTargetColumnsNotSet()
    {
        String configYaml = "" +
                "type: convert_unicode_sequence_to_string\n" +
                "target_columns:\n";
        ConfigSource config = getConfigFromYaml(configYaml);

        exception.expect(ConfigException.class);
        exception.expectMessage("Setting null to a task field is not allowed.");
        config.loadConfig(PluginTask.class);
    }

    @Test
    public void testColumnIncludingEscapeSequenceActuallyConverted()
    {
        String configYaml = "" +
                "type: convert_unicode_sequence_to_string\n" +
                "target_columns:\n" +
                "- column1\n";

        ConfigSource config = getConfigFromYaml(configYaml);

        final Schema inputSchema = Schema.builder()
            .add("column1", STRING)
            .add("column2", STRING)
            .build();

        ConvertUnicodeSequenceToStringFilterPlugin convertUnicodeSequencePlugin = new ConvertUnicodeSequenceToStringFilterPlugin();
        convertUnicodeSequencePlugin.transaction(config, inputSchema, new Control()
        {
            @Override
            public void run(TaskSource taskSource, Schema outputSchema)
            {
                MockPageOutput mockPageOutput = new MockPageOutput();
                PageOutput pageOutput = convertUnicodeSequencePlugin.open(taskSource,
                                                                    inputSchema,
                                                                    outputSchema,
                                                                    mockPageOutput);

                for (Page page : PageTestUtils.buildPage(runtime.getBufferAllocator(),
                        inputSchema,
                        "column1Val\u0041", "column2Val\u0041"
                    )) {
                        pageOutput.add(page);
                    }

                pageOutput.finish();
                pageOutput.close();

                PageReader pageReader = new PageReader(outputSchema);
                try {
                    for (Page page : mockPageOutput.pages) {
                        pageReader.setPage(page);
                        System.out.println(outputSchema.getColumn(0));
                        assertEquals("column1ValA", pageReader.getString(outputSchema.getColumn(0)));
                        assertEquals("column2Val\u0041", pageReader.getString(outputSchema.getColumn(1)));
                    }
                }
                finally {
                    pageReader.close();
                }
            }
        });
    }
}

Embulk::JavaPlugin.register_filter(
  "convert_unicode_sequence_to_string", "org.embulk.filter.convert_unicode_sequence_to_string.ConvertUnicodeSequenceToStringFilterPlugin",
  File.expand_path('../../../../classpath', __FILE__))

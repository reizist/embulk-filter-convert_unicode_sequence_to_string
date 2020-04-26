package org.embulk.filter.convert_unicode_sequence_to_string;

import org.embulk.filter.convert_unicode_sequence_to_string.ConvertUnicodeSequenceToStringFilterPlugin.PluginTask;

import org.embulk.spi.Column;
import org.embulk.spi.Schema;
import org.embulk.spi.ColumnVisitor;
import org.embulk.spi.PageBuilder;
import org.embulk.spi.PageReader;

public class ColumnVisitorImpl implements ColumnVisitor
{
    private final PluginTask task;
    private final PageReader pageReader;
	private final PageBuilder pageBuilder;

	ColumnVisitorImpl(PluginTask task, PageReader pageReader, PageBuilder pageBuilder)
	{
		this.task = task;
		this.pageReader = pageReader;
		this.pageBuilder = pageBuilder;
	}

	public boolean visitColumns(Schema inputSchema)
	{
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
			for (String targetColumnName : task.getTargetColumns()) {
				if (column.getName().equals(targetColumnName)) {
					pageBuilder.setString(column, convert(pageReader.getString(column)));
				}
				else {
					pageBuilder.setString(column, pageReader.getString(column));
				}
			}
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

	private String convert(String text)
	{
		text = text.replace("\\", "//");
		String[] arr = text.split("//u");
		String output = "";
		for (int i = 0; i < arr.length; i++) {
			try {
				int hexVal = Integer.parseInt(arr[i], 16);
				output += Character.toString((char) hexVal);
			}
			catch (NumberFormatException e) {
				output += arr[i];
			}
		}
		return output;
	}
}

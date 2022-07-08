package com.duckdigit.ai.infinity;

import java.util.ArrayList;

public interface ISAMDefiner
{
	String getFile();

	String getLayout();

	String getIndexFile();

	String getIndexLayout();

	int[] getVarTypes();

	String getFilterTemplate();

	String getRangeTemplate();

	ArrayList getPresets();

	ArrayList getAdvsets();

	int getCacheSize();
}

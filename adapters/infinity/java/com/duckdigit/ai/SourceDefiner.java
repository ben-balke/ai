package com.duckdigit.ai;

import java.util.ArrayList;

public interface SourceDefiner {
	String getClassString();

	int getVarCount();

	int[] getVarTypes();

	int[] getVarLengths();

	ArrayList getVarDecls();

	ArrayList getVarNames();

	ArrayList getKeyNames();

	boolean checkUpdate();

	boolean checkKey(
		    String      p_sField );

	int getUpdateIndex(
		    String      p_sField );
}

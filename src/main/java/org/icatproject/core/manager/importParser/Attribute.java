package org.icatproject.core.manager.importParser;

public class Attribute {

		public Integer getFieldNum() {
			return fieldNum;
		}

		public String getType() {
			return type;
		}

		private Integer fieldNum;
		private String type;

		public Attribute(Integer fieldNum, String type) {
			this.fieldNum = fieldNum;
			this.type = type;
		}
	}
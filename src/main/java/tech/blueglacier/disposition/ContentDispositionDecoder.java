package tech.blueglacier.disposition;

import tech.blueglacier.Common;
import com.sun.mail.util.PropUtil;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.util.CharsetUtil;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class ContentDispositionDecoder {
	private static boolean decodeParametersStrict = PropUtil.getBooleanSystemProperty("mail.mime.decodeparameters.strict", false);

	private static ContentDispositionHeaderValue decodeContentDisposition(
			String headerValue) throws MimeException {
		ContentDispositionHeaderValue contentDispositionHeaderValue = new ContentDispositionHeaderValue();
		contentDispositionHeaderValue.setValue(headerValue);
		try {
			int i = headerValue.indexOf('\'');
			if (i <= 0) {
				if (decodeParametersStrict) {
					throw new MimeException(
							"Missing charset in encoded value: " + headerValue);
				}
				return contentDispositionHeaderValue;
			}
			
			String charset = headerValue.substring(0, i);
			charset = Common.getFallbackCharset(charset);
			if (CharsetUtil.lookup(charset) == null) {
				return contentDispositionHeaderValue;
			}	
			
			int li = headerValue.indexOf('\'', i + 1);
			if (li < 0) {
				if (decodeParametersStrict) {
					throw new MimeException(
							"Missing language in encoded value: " + headerValue);
				}
				return contentDispositionHeaderValue;
			}
			headerValue = headerValue.substring(li + 1);
			contentDispositionHeaderValue.setCharset(charset);
			contentDispositionHeaderValue.setValue(decodeBytes(headerValue, charset));
		} catch (NumberFormatException nex) {
			if (decodeParametersStrict) {
				throw new MimeException(nex);
			}
		} catch (StringIndexOutOfBoundsException ex) {
			if (decodeParametersStrict) {
				throw new MimeException(ex);
			}
		}
		return contentDispositionHeaderValue;
	}

	private static String decodeBytes(String value, String charset)
			throws MimeException {
		byte[] b = new byte[value.length()];

		int i = 0;
		int temp = 0;
		for (int bi = 0; i < value.length(); ++i) {
			char c = value.charAt(i);
			if (c == '%') {
				String hex = value.substring(i + 1, i + 3);
				c = (char) Integer.parseInt(hex, 16);
				i += 2;
			}
			b[(bi++)] = (byte) c;
			temp = bi;
		}
		
		String str;
		try {
			str = new String(b, 0, temp, charset);
		} catch (UnsupportedEncodingException e) {
			throw new MimeException(e);
		}
		return str;
	}

	/**
	 * Logical class for representing Content-Disposition header value
	 */
	private static class ContentDispositionHeaderValue {
		private String value;
		private String charset;	

		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		public String getCharset() {
			return charset;
	}
		public void setCharset(String charset) {
			this.charset = charset;
		}
	}

	public static String decodeDispositionFileName(Map<String, String> contentDispositionParameters) throws MimeException {
		// Refer RFC 2183 'The Content-Disposition Header Field' and
		// RFC 2184 'Parameter Value Character Set and Language Information'

		Set<String> contentDispositionKeySet = contentDispositionParameters.keySet();
		String fileName = null;
		if (contentDispositionKeySet != null) {
			String[] sortedDispositionFileNameKeys = getSortedStringArray(contentDispositionKeySet.toArray());
			StringBuilder valueStr = new StringBuilder();
			for (int i = 0; i < sortedDispositionFileNameKeys.length; i++) {
				valueStr.append(contentDispositionParameters.get(sortedDispositionFileNameKeys[i]));
			}
			String encodedStr = valueStr.toString();
			if (!encodedStr.isEmpty()) {
				fileName = decodeContentDisposition(encodedStr).getValue();
			}
		}
		return fileName;
	}
	
	private static String[] getSortedStringArray(Object[] objArray) {
		String[] strArray = new String[objArray.length];
		for (int i = 0; i < objArray.length; i++) {
			strArray[i] = (String) objArray[i];
		}
		Arrays.sort(strArray);
		return strArray;
	}
}

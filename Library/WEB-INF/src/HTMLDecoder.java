
public class HTMLDecoder {
	public boolean isLastError = false;
	public String Decode(String Input) {
		isLastError = false;
		StringBuffer StrBuf = new StringBuffer();
		char[] cs = Input.toCharArray();
		for (int i = 0; i < cs.length; i++) {
			if (cs[i] == '&' && i + 1 < cs.length && (cs[i + 1] == '#')) {
				int j = i + 2;
				try {
					int t = 0;
					while(cs[j] != ';') {
						t = t * 10 + cs[j] - '0';
						j++;
					}
					StrBuf.append((char)t);
					
				} catch (Exception e) {
					isLastError = true;
				} finally {
					i = j;
				}
			}
			else
				StrBuf.append(cs[i]);
		}
		
		return new String(StrBuf);
	}
}

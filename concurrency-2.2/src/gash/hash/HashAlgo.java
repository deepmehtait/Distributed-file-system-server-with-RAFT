package gash.hash;

import java.util.Properties;

public interface HashAlgo {
	void init(Properties conf);

	Long hash(String value);
}

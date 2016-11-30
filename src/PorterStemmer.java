import java.util.regex.Pattern;

public class PorterStemmer {

	// a single consonant
	private static final String c = "[^aeiou]";
	// a single vowel
	private static final String v = "[aeiouy]";

	// a sequence of consonants; the second/third/etc consonant cannot be 'y'
	private static final String C = c + "[^aeiouy]*";
	// a sequence of vowels; the second/third/etc cannot be 'y'
	private static final String V = v + "[aeiou]*";

	// this regex pattern tests if the token has measure > 0 [at least one VC].
	// ^ beginning of string, ? = 0 or 1 but C is multiple c's so its matching
	// and throwing away any c's
	private static final Pattern mGr0 = Pattern
			.compile("^(" + C + ")?" + V + C);

	// add more Pattern variables for the following patterns:
	// m equals 1: token has measure == 1
	private static final Pattern mE01 = Pattern.compile("^(" + C + ")?" + V + C
			+ "(" + V + ")?$");

	private static final Pattern mGr1 = Pattern.compile("^(" + C + ")?" + V + C
			+ V + C);

	// vowel: token has a vowel after the first (optional) C

	private static final Pattern vowel = Pattern.compile("^(" + C + ")?" + V);
	// double consonant: token ends in two consonants that are the same,
	// unless they are L, S, or Z. (look up "backreferencing" to help
	// with this)
	private static final Pattern dc = Pattern.compile("([^aeioulsz])\\1$");

	// double cononant w/ l. Needed for final case e.g. controller -> control
	private static final Pattern dcwl = Pattern.compile("([l])\\1$");

	// m equals 1, cvc: token is in Cvc form, where the last c is not w, x,
	// or y.

	private static final Pattern staro = Pattern.compile("^(" + C + ")" + v
			+ "[^aeiouwxy]$");

	private static String stem;

	private static boolean complete;

	public static String processToken(String token) {
		if (token.length() < 3) {
			return token; // token must be at least 3 chars
		}
		// step 1a
		if (token.endsWith("sses")) {
			token = token.substring(0, token.length() - 2);
		}

		else if (token.endsWith("ies")) {
			token = token.substring(0, token.length() - 2);
		}

		else if (Pattern.compile("([^s])" + "[s]$").matcher(token).find()) {
			token = token.substring(0, token.length() - 1);// what?
		}

		// step 1b
		boolean doStep1bb = false;
		// step 1b
		if (token.endsWith("eed")) { // 1b.1
			// if that has m>0, then remove the "d".
			stem = token.substring(0, token.length() - 3);
			if (mGr0.matcher(stem).find()) {
				// if the pattern matches the stem
				token = stem + "ee";
			}
		}

		else if (token.endsWith("ed")) {
			stem = token.substring(0, token.length() - 2);
			if (vowel.matcher(stem).find()) {
				token = stem;
				doStep1bb = true;
			}
		}

		else if (token.endsWith("ing")) {
			stem = token.substring(0, token.length() - 3);
			if (vowel.matcher(stem).find()) {
				token = stem;
				doStep1bb = true;
			}
		}

		// step 1b*, only if the 1b.2 or 1b.3 were performed.
		if (doStep1bb) {
			if (token.endsWith("at") || token.endsWith("bl")
					|| token.endsWith("iz")) {

				token = token + "e";
			}
			// use the regex patterns you wrote for 1b*.4 and 1b*.5
			else if (dc.matcher(token).find()) {
				token = token.substring(0, token.length() - 1);
			} else if (staro.matcher(token).find()) {
				token = token + "e";
			}
		}

		// step 1c
		// program this step. test the suffix of 'y' first, then test the
		// condition *v* on the stem.
		if (token.endsWith("y")) {
			stem = token.substring(0, token.length() - 1);
			if (vowel.matcher(stem).find()) {
				token = stem + "i";
			}
		}

		// step 2
		// program this step. for each suffix, see if the token ends in the
		// suffix.

		String[][] step2pairs = { { "ational", "ate" }, { "tional", "tion" },
				{ "enci", "ence" }, { "anci", "ance" }, { "izer", "ize" },
				{ "iser", "ize" }, { "abli", "able" }, { "alli", "al" },
				{ "entli", "ent" }, { "eli", "e" }, { "ousli", "ous" },
				{ "ization", "ize" }, { "isation", "ize" }, { "ation", "ate" },
				{ "ator", "ate" }, { "alism", "al" }, { "iveness", "ive" },
				{ "fulness", "ful" }, { "ousness", "ous" }, { "aliti", "al" },
				{ "iviti", "ive" }, { "biliti", "ble" } };

		complete = false;

		for (int i = 0; i < step2pairs.length && !(complete); i++) {
			if (token.endsWith(step2pairs[i][0])) {
				stem = token.substring(0,
						token.length() - step2pairs[i][0].length());
				complete = true;
				if (mGr0.matcher(stem).find()) {
					token = stem + step2pairs[i][1];
				}

			}
		}
		// step 3
		// program this step. the rules are identical to step 2 and you can use
		// the same helper method. you may also want a matrix here.

		String[][] step3pairs = { { "icate", "ic" }, { "ative", "" },
				{ "alize", "al" }, { "alise", "al" }, { "iciti", "ic" },
				{ "ical", "ic" }, { "ful", "" }, { "ness", "" } };
		complete = false;
		for (int i = 0; i < step3pairs.length && !(complete); i++) {
			if (token.endsWith(step3pairs[i][0])) {
				stem = token.substring(0,
						token.length() - step3pairs[i][0].length());
				complete = true;
				if (mGr0.matcher(stem).find()) {
					token = stem + step3pairs[i][1];
				}
			}
		}

		// step 4
		// program this step similar to step 2/3, except now the stem must have
		// measure > 1.
		// note that ION should only be removed if the suffix is SION or TION,
		// which would leave the S or T.
		// as before, if one suffix matches, do not try any others even if the
		// stem does not have measure > 1.

		complete = false;

		String[] step4suffixes = { "al", "ance", "ence", "er", "ic", "able",
				"ible", "ant", "ement", "ment", "ent", "ion", "ou", "ism",
				"ate", "iti", "ous", "ive", "ize", "ise" };

		for (int i = 0; i < step4suffixes.length && !(complete); i++) {
			if (token.endsWith(step4suffixes[i])) {
				stem = token.substring(0,
						token.length() - step4suffixes[i].length());
				complete = true;
				if (step4suffixes[i].equals("ion")
						&& !(stem.endsWith("s") || stem.endsWith("t")))
					continue;// try doing the opposite with "sion", "tion" then
								// just adding t or s
				if (mGr1.matcher(stem).find()) {
					token = stem;
				}
			}
		}

		// step 5
		// program this step. you have a regex for m=1 and for "Cvc", which
		// you can use to see if m=1 and NOT Cvc.
		// all your code should change the variable token, which represents
		// the stemmed term for the token.

		if (mGr1.matcher(token).find() && token.endsWith("e")) {
			token = token.substring(0, token.length() - 1);
		}

		else if (mE01.matcher(token).find() && !(staro.matcher(token).find())
				&& token.endsWith("e")) {
			token = token.substring(0, token.length() - 1);
		}

		if (mGr1.matcher(token).find() && dcwl.matcher(token).find())
			token = token.substring(0, token.length() - 1);
		return token;
	}
}
package util;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestQuote {

	@Test
	public void test() {
		String in = "   asdf <br>lorem <br><br> ipsum\nqwerty   123  <p>   &gt;<p>";
		String out = Quote.quote(in);
		String expected = "> asdf\n> lorem\n> ipsum qwerty 123\n> \n> >\n";
		org.junit.Assert.assertEquals(out, expected);
	}
}

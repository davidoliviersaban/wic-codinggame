import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestScore {

	@Test
	public void testNegativeScore() {

		String[] inputMatchString = {

		"MatchConstants: 3 10 Point +11187.00 +05449.00 Vector +00000.00 +00000.00 11 Point +07231.00 +06644.00 Vector +00000.00 +00000.00 12 Point +05412.00 +02838.00 Vector +00000.00 +00000.00 13 Point +10322.00 +03377.00 Vector +00000.00 +00000.00 ", };

		String[] inputGameStateString = {

				"GameState 47 13 1 false false",
				"Pod 0 Point +11941.00 +04507.00 Vector +00445.00 +00196.00 +225.00 13 0 1 true",
				"Pod 1 Point +10117.00 +02261.00 Vector +00244.00 +00157.00 +071.00 13 0 1 true",
				"Pod 2 Point +07250.00 +06081.00 Vector -00300.00 -00220.00 +252.00 12 0 2 false",
				"Pod 3 Point +10405.00 +03955.00 Vector +00182.00 +00089.00 +083.00 10 0 1 false", };

		TestUtils.setGameConstantsFromDebug(inputMatchString);

		Player.GameState inputGameState = TestUtils.getGameStateFromDebug(inputGameStateString);
		double score = Player.firstPodsScore.getGameStateScore(inputGameState);

		TestUtils.out("Score: " + score);
		assertTrue("Score should be negative as I'm way behind", score < 0);
	}

}
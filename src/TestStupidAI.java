import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestStupidAI {

	public static Player.AI ai = Player.stupidAI;

	@Test
	public void testComputeNotNull() {

		String[] inputMatchString = {

		"MatchConstants: 3 10 Point +11227.00 +05445.00 Vector +00000.00 +00000.00 11 Point +07246.00 +06666.00 Vector +00000.00 +00000.00 12 Point +05411.00 +02816.00 Vector +00000.00 +00000.00 13 Point +10326.00 +03392.00 Vector +00000.00 +00000.00 ", };

		String[] inputGameStateString = {

				"GameState 6 5 5 false false",
				"Pod 0 Point +08381.00 +06911.00 Vector -00491.00 +00138.00 +192.00 11 0 0 false",
				"Pod 1 Point +08740.00 +05412.00 Vector -00288.00 +00019.00 +156.00 11 0 0 false",
				"Pod 2 Point +11662.00 +06879.00 Vector +00000.00 +00000.00 +183.00 11 0 0 false",
				"Pod 3 Point +10782.00 +04016.00 Vector +00000.00 +00000.00 +143.00 11 0 0 false", };

		TestUtils.setGameConstantsFromDebug(inputMatchString);

		Player.GameState inputGameState = TestUtils.getGameStateFromDebug(inputGameStateString);
		Player.Action[] actions = ai.compute(inputGameState);

		assertTrue(actions != null);
	}

}
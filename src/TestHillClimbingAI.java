import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestHillClimbingAI {

	public static Player.HillClimbingAI ai = Player.hillClimbingAI;

	@Test
	public void testComputeNotNull() {

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
		Player.Action[] actions = ai.compute(inputGameState);

		assertTrue("Actions should not be null", actions != null);
	}

	@Test
	public void testComputeFirstPodShouldGoAt100() {

		// seed=238119196
		// pod_per_player=2
		// pod_timeout=100
		// map=13935 1924 8029 3287 2664 7018 10043 5980

		TestUtils.out("Starting testComputeFirstPodShouldGoAt100");

		String[] inputMatchString = {

		"MatchConstants: 3 10 Point +13935.00 +01924.00 Vector +00000.00 +00000.00 11 Point +08029.00 +03287.00 Vector +00000.00 +00000.00 12 Point +02664.00 +07018.00 Vector +00000.00 +00000.00 13 Point +10043.00 +05980.00 Vector +00000.00 +00000.00 ", };

		String[] inputGameStateString = {

				"GameState 11 2 10 false false",
				"Pod 0 Point +07185.00 +03495.00 Vector -00543.00 +00128.00 +147.00 12 0 1 true",
				"Pod 1 Point +08875.00 +02452.00 Vector -00325.00 +00102.00 +146.00 11 0 1 true",
				"Pod 2 Point +08624.00 +03633.00 Vector -00508.00 +00054.00 +147.00 11 0 1 true",
				"Pod 3 Point +10383.00 +02087.00 Vector -00402.00 +00204.00 +153.00 11 0 1 true", };

		TestUtils.setGameConstantsFromDebug(inputMatchString);

		Player.GameState inputGameState = TestUtils.getGameStateFromDebug(inputGameStateString);
		// Player.Time.noTimeLimit = true;
		Player.Action[] actions = ai.computeAction(inputGameState);

		TestUtils.printAction(actions[0]);

		assertTrue("Actions should not be null", actions != null);
		assertEquals(100, actions[0].thrust);
	}

}
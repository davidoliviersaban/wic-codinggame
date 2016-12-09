import static org.junit.Assert.*;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestEngine {

	@Test
	public void test001FirstMove() {

		String[] inputMatchString = {

		"MatchConstants: 3 10 Point +05945.00 +04232.00 Vector +00000.00 +00000.00 11 Point +14684.00 +01412.00 Vector +00000.00 +00000.00 12 Point +03479.00 +07217.00 Vector +00000.00 +00000.00 13 Point +09422.00 +07237.00 Vector +00000.00 +00000.00 ", };

		String[] inputGameStateString = {

				"GameState 1 0 0 0 0 false false",
				"Pod 0 Point +05791.00 +03756.00 Vector +00000.00 +00000.00 -001.00 11 0",
				"Pod 1 Point +06099.00 +04708.00 Vector +00000.00 +00000.00 -001.00 11 0",
				"Pod 2 Point +05484.00 +02804.00 Vector +00000.00 +00000.00 -001.00 11 0",
				"Pod 3 Point +06406.00 +05660.00 Vector +00000.00 +00000.00 -001.00 11 0", };

		String[] myInputActions = { "Action BOOST Point +14684.00 +01412.00 650", "Action BOOST Point +14684.00 +01412.00 650", };

		String[] opInputActions = { "Action MOVE Point +14684.00 +01412.00 1", "Action MOVE Point +14684.00 +01412.00 1", };

		String[] actualGameStateString = {

				"GameState 2 1 1 0 0 false false",
				"Pod 0 Point +06420.00 +03590.00 Vector +00534.00 -00140.00 +345.00 11 0",
				"Pod 1 Point +06706.00 +04475.00 Vector +00515.00 -00198.00 +339.00 11 0",
				"Pod 2 Point +05485.00 +02804.00 Vector +00000.00 +00000.00 +351.00 11 0",
				"Pod 3 Point +06407.00 +05660.00 Vector +00000.00 +00000.00 +333.00 11 0", };

		TestUtils.setGameConstantsFromDebug(inputMatchString);

		Player.GameState inputGameState = TestUtils.getGameStateFromDebug(inputGameStateString);
		Player.GameState actualGameState = TestUtils.getGameStateFromDebug(actualGameStateString);
		Player.Action[] myActions = TestUtils.getActionFromDebug(myInputActions);
		Player.Action[] opActions = TestUtils.getActionFromDebug(opInputActions);

		Player.GameState computedGameState = Player.GameEngine.applyAction(inputGameState, myActions, opActions);

		TestUtils.printComputedVSActual(computedGameState, actualGameState);

		assertNotNull(computedGameState);
		assertEquals(actualGameState, computedGameState);

	}

	@Test
	public void test001SimpleMove() {

		// seed=562710226
		// pod_per_player=2
		// pod_timeout=100
		// map=11227 5445 7246 6666 5411 2816 10326 3392

		String[] inputMatchString = {

		"MatchConstants: 3 10 Point +11227.00 +05445.00 Vector +00000.00 +00000.00 11 Point +07246.00 +06666.00 Vector +00000.00 +00000.00 12 Point +05411.00 +02816.00 Vector +00000.00 +00000.00 13 Point +10326.00 +03392.00 Vector +00000.00 +00000.00 ", };

		String[] inputGameStateString = {

				"GameState 8 0 7 0 0 false false",
				"Pod 0 Point +07293.00 +07034.00 Vector -00448.00 +00021.00 +228.16 12 0",
				"Pod 1 Point +08208.00 +05447.00 Vector -00207.00 +00013.00 +156.10 11 0",
				"Pod 2 Point +11660.00 +06879.00 Vector +00000.00 +00000.00 +182.76 11 0",
				"Pod 3 Point +10780.00 +04018.00 Vector +00000.00 +00000.00 +143.15 11 0", };

		String[] myInputActions = { "Action MOVE Point +05859.00 +02795.00 100", "Action MOVE Point +08208.00 +05447.00 0", };

		String[] opInputActions = { "Action MOVE Point +07246.00 +06666.00 1", "Action MOVE Point +07246.00 +06666.00 1", };

		String[] actualGameStateString = {

				"GameState 9 1 8 0 0 false false",
				"Pod 0 Point +06805.00 +06964.00 Vector -00415.00 -00059.00 +246.16 12 0",
				"Pod 1 Point +08001.00 +05460.00 Vector -00175.00 +00011.00 +156.10 11 0",
				"Pod 2 Point +11659.00 +06879.00 Vector +00000.00 +00000.00 +182.76 11 0",
				"Pod 3 Point +10779.00 +04019.00 Vector +00000.00 +00000.00 +143.16 11 0", };

		TestUtils.setGameConstantsFromDebug(inputMatchString);

		Player.GameState inputGameState = TestUtils.getGameStateFromDebug(inputGameStateString);
		Player.GameState actualGameState = TestUtils.getGameStateFromDebug(actualGameStateString);
		Player.Action[] myActions = TestUtils.getActionFromDebug(myInputActions);
		Player.Action[] opActions = TestUtils.getActionFromDebug(opInputActions);

		Player.GameState computedGameState = Player.GameEngine.applyAction(inputGameState, myActions, opActions);

		TestUtils.printComputedVSActual(computedGameState, actualGameState);

		assertNotNull(computedGameState);
		assertEquals(actualGameState, computedGameState);

	}

	@Test
	public void test001TurnAround() {

		// seed=875641117
		// pod_per_player=2
		// pod_timeout=100
		// map=10047 5950 13946 1962 8046 3272 2658 7002

		String[] inputMatchString = {

		"MatchConstants: 3 10 Point +10047.00 +05950.00 Vector +00000.00 +00000.00 11 Point +13946.00 +01962.00 Vector +00000.00 +00000.00 12 Point +08046.00 +03272.00 Vector +00000.00 +00000.00 13 Point +02658.00 +07002.00 Vector +00000.00 +00000.00 ", };

		String[] inputGameStateString = {

				"GameState 9 8 8 0 0 false false",
				"Pod 0 Point +13292.00 +02024.00 Vector +00397.00 -00298.00 +355.00 11 0",
				"Pod 1 Point +12789.00 +04348.00 Vector +00144.00 -00093.00 +309.00 11 0",
				"Pod 2 Point +08982.00 +04893.00 Vector +00000.00 +00000.00 +329.00 11 0",
				"Pod 3 Point +11120.00 +06991.00 Vector +00000.00 +00000.00 +299.00 11 0", };

		String[] myInputActions = { "Action MOVE Point +13549.00 +02260.00 77", "Action MOVE Point +12789.00 +04348.00 0", };

		String[] opInputActions = { "Action MOVE Point +13946.00 +01962.00 1", "Action MOVE Point +13946.00 +01962.00 1", };

		String[] actualGameStateString = {

				"GameState 10 0 9 0 0 false false",
				"Pod 0 Point +13764.00 +01743.00 Vector +00401.00 -00239.00 +013.00 12 0",
				"Pod 1 Point +12933.00 +04255.00 Vector +00122.00 -00079.00 +309.00 11 0",
				"Pod 2 Point +08983.00 +04892.00 Vector +00000.00 +00000.00 +329.00 11 0",
				"Pod 3 Point +11120.00 +06990.00 Vector +00000.00 +00000.00 +299.00 11 0", };

		TestUtils.setGameConstantsFromDebug(inputMatchString);

		Player.GameState inputGameState = TestUtils.getGameStateFromDebug(inputGameStateString);
		Player.GameState actualGameState = TestUtils.getGameStateFromDebug(actualGameStateString);
		Player.Action[] myActions = TestUtils.getActionFromDebug(myInputActions);
		Player.Action[] opActions = TestUtils.getActionFromDebug(opInputActions);

		Player.GameState computedGameState = Player.GameEngine.applyAction(inputGameState, myActions, opActions);

		TestUtils.printComputedVSActual(computedGameState, actualGameState);

		assertNotNull(computedGameState);
		assertEquals(actualGameState, computedGameState);

	}

	@Test
	public void test001TurnAround2() {

		// seed=875641117
		// pod_per_player=2
		// pod_timeout=100
		// map=10047 5950 13946 1962 8046 3272 2658 7002

		String[] inputMatchString = {

		"MatchConstants: 3 10 Point +10047.00 +05950.00 Vector +00000.00 +00000.00 11 Point +13946.00 +01962.00 Vector +00000.00 +00000.00 12 Point +08046.00 +03272.00 Vector +00000.00 +00000.00 13 Point +02658.00 +07002.00 Vector +00000.00 +00000.00 ", };

		String[] inputGameStateString = {

				"GameState 45 0 44 0 0 false false",
				"Pod 0 Point +02644.00 +06609.00 Vector -00304.00 +00344.00 +095.00 10 0",
				"Pod 1 Point +13676.00 +03793.00 Vector +00000.00 +00000.00 +309.00 11 0",
				"Pod 2 Point +09018.00 +04857.00 Vector +00000.00 +00000.00 +330.00 11 0",
				"Pod 3 Point +11120.00 +06955.00 Vector +00000.00 +00000.00 +300.00 11 0", };

		String[] myInputActions = { "Action MOVE Point +10351.00 +05606.00 0", "Action MOVE Point +13676.00 +03793.00 0", };

		String[] opInputActions = { "Action MOVE Point +13946.00 +01962.00 1", "Action MOVE Point +13946.00 +01962.00 1", };

		String[] actualGameStateString = {

				"GameState 46 1 45 0 0 false false",
				"Pod 0 Point +02340.00 +06953.00 Vector -00258.00 +00292.00 +077.00 10 0",
				"Pod 1 Point +13676.00 +03793.00 Vector +00000.00 +00000.00 +309.00 11 0",
				"Pod 2 Point +09019.00 +04856.00 Vector +00000.00 +00000.00 +330.00 11 0",
				"Pod 3 Point +11120.00 +06954.00 Vector +00000.00 +00000.00 +300.00 11 0", };

		TestUtils.setGameConstantsFromDebug(inputMatchString);

		Player.GameState inputGameState = TestUtils.getGameStateFromDebug(inputGameStateString);
		Player.GameState actualGameState = TestUtils.getGameStateFromDebug(actualGameStateString);
		Player.Action[] myActions = TestUtils.getActionFromDebug(myInputActions);
		Player.Action[] opActions = TestUtils.getActionFromDebug(opInputActions);

		Player.GameState computedGameState = Player.GameEngine.applyAction(inputGameState, myActions, opActions);

		TestUtils.printComputedVSActual(computedGameState, actualGameState);

		assertNotNull(computedGameState);
		assertEquals(actualGameState, computedGameState);

	}

	@Test
	public void test002SimpleCollisionBetweenPods() {

		// seed=833152081
		// pod_per_player=2
		// pod_timeout=100
		// map=3146 7531 9504 4370 14491 7802 6294 4271 7800 855 7668 5962

		String[] inputMatchString = {

		"MatchConstants: 3 10 Point +03146.00 +07531.00 Vector +00000.00 +00000.00 11 Point +09504.00 +04370.00 Vector +00000.00 +00000.00 12 Point +14491.00 +07802.00 Vector +00000.00 +00000.00 13 Point +06294.00 +04271.00 Vector +00000.00 +00000.00 14 Point +07800.00 +00855.00 Vector +00000.00 +00000.00 15 Point +07668.00 +05962.00 Vector +00000.00 +00000.00 ", };

		String[] inputGameStateString = {

				"GameState 3 2 2 0 0 false false",
				"Pod 0 Point +04126.00 +06587.00 Vector +00512.00 -00210.00 +338.00 11 0",
				"Pod 1 Point +04405.00 +07369.00 Vector +00404.00 -00238.00 +330.00 11 0",
				"Pod 2 Point +02480.00 +06188.00 Vector +00000.00 +00000.00 +345.00 11 0",
				"Pod 3 Point +03816.00 +08872.00 Vector +00000.00 +00000.00 +322.00 11 0", };

		String[] myInputActions = { "Action MOVE Point +08992.00 +04580.00 100", "Action MOVE Point +04405.00 +07369.00 0", };

		String[] opInputActions = { "Action MOVE Point +09504.00 +04370.00 1", "Action MOVE Point +09504.00 +04370.00 1", };

		String[] actualGameStateString = {

				"GameState 4 3 3 0 0 false false",
				"Pod 0 Point +04725.00 +06307.00 Vector +00495.00 -00320.00 +338.00 11 0",
				"Pod 1 Point +04814.00 +07163.00 Vector +00361.00 -00092.00 +330.00 11 0",
				"Pod 2 Point +02481.00 +06188.00 Vector +00000.00 +00000.00 +345.00 11 0",
				"Pod 3 Point +03817.00 +08871.00 Vector +00000.00 +00000.00 +322.00 11 0", };

		TestUtils.setGameConstantsFromDebug(inputMatchString);

		Player.GameState inputGameState = TestUtils.getGameStateFromDebug(inputGameStateString);
		Player.GameState actualGameState = TestUtils.getGameStateFromDebug(actualGameStateString);
		Player.Action[] myActions = TestUtils.getActionFromDebug(myInputActions);
		Player.Action[] opActions = TestUtils.getActionFromDebug(opInputActions);

		Player.GameState computedGameState = Player.GameEngine.applyAction(inputGameState, myActions, opActions);

		TestUtils.printComputedVSActual(computedGameState, actualGameState);

		assertNotNull(computedGameState);
		assertEquals(actualGameState, computedGameState);

	}

	@Test
	public void test003NoCollisionPodVsCheckPoint() {

		// seed=562710226
		// pod_per_player=2
		// pod_timeout=100
		// map=11227 5445 7246 6666 5411 2816 10326 3392

		String[] inputMatchString = {

		"MatchConstants: 3 10 Point +11227.00 +05445.00 Vector +00000.00 +00000.00 11 Point +07246.00 +06666.00 Vector +00000.00 +00000.00 12 Point +05411.00 +02816.00 Vector +00000.00 +00000.00 13 Point +10326.00 +03392.00 Vector +00000.00 +00000.00 ", };

		String[] inputGameStateString = {

				"GameState 6 5 5 0 0 false false",
				"Pod 0 Point +08381.00 +06911.00 Vector -00491.00 +00138.00 +192.00 11 0",
				"Pod 1 Point +08740.00 +05412.00 Vector -00288.00 +00019.00 +156.00 11 0",
				"Pod 2 Point +11662.00 +06879.00 Vector +00000.00 +00000.00 +183.00 11 0",
				"Pod 3 Point +10782.00 +04016.00 Vector +00000.00 +00000.00 +143.00 11 0", };

		String[] myInputActions = { "Action MOVE Point +07737.00 +06528.00 80", "Action MOVE Point +08740.00 +05412.00 0", };

		String[] opInputActions = { "Action MOVE Point +07246.00 +06666.00 1", "Action MOVE Point +07246.00 +06666.00 1", };

		String[] actualGameStateString = {

				"GameState 7 6 6 0 0 false false",
				"Pod 0 Point +07821.00 +07009.00 Vector -00476.00 +00083.00 +210.00 11 0",
				"Pod 1 Point +08452.00 +05431.00 Vector -00244.00 +00016.00 +156.00 11 0",
				"Pod 2 Point +11661.00 +06879.00 Vector +00000.00 +00000.00 +183.00 11 0",
				"Pod 3 Point +10781.00 +04017.00 Vector +00000.00 +00000.00 +143.00 11 0", };

		TestUtils.setGameConstantsFromDebug(inputMatchString);

		Player.GameState inputGameState = TestUtils.getGameStateFromDebug(inputGameStateString);
		Player.GameState actualGameState = TestUtils.getGameStateFromDebug(actualGameStateString);
		Player.Action[] myActions = TestUtils.getActionFromDebug(myInputActions);
		Player.Action[] opActions = TestUtils.getActionFromDebug(opInputActions);

		Player.GameState computedGameState = Player.GameEngine.applyAction(inputGameState, myActions, opActions);

		TestUtils.printComputedVSActual(computedGameState, actualGameState);

		assertNotNull(computedGameState);
		assertEquals(actualGameState, computedGameState);

	}

	@Test
	public void test004SimpleCollisionPodVsCheckPoint() {

		// seed=562710226
		// pod_per_player=2
		// pod_timeout=100
		// map=11227 5445 7246 6666 5411 2816 10326 3392

		String[] inputMatchString = {

		"MatchConstants: 3 10 Point +11227.00 +05445.00 Vector +00000.00 +00000.00 11 Point +07246.00 +06666.00 Vector +00000.00 +00000.00 12 Point +05411.00 +02816.00 Vector +00000.00 +00000.00 13 Point +10326.00 +03392.00 Vector +00000.00 +00000.00 ", };

		String[] inputGameStateString = {

				"GameState 7 6 6 0 0 false false",
				"Pod 0 Point +07821.00 +07009.00 Vector -00476.00 +00083.00 +210.00 11 0",
				"Pod 1 Point +08452.00 +05431.00 Vector -00244.00 +00016.00 +156.00 11 0",
				"Pod 2 Point +11661.00 +06879.00 Vector +00000.00 +00000.00 +183.00 11 0",
				"Pod 3 Point +10781.00 +04017.00 Vector +00000.00 +00000.00 +143.00 11 0", };

		String[] myInputActions = { "Action MOVE Point +07722.00 +06583.00 78", "Action MOVE Point +08452.00 +05431.00 0", };

		String[] opInputActions = { "Action MOVE Point +07246.00 +06666.00 1", "Action MOVE Point +07246.00 +06666.00 1", };

		String[] actualGameStateString = {

				"GameState 8 0 7 0 0 false false",
				"Pod 0 Point +07293.00 +07034.00 Vector -00448.00 +00021.00 +228.00 12 0",
				"Pod 1 Point +08208.00 +05447.00 Vector -00207.00 +00013.00 +156.00 11 0",
				"Pod 2 Point +11660.00 +06879.00 Vector +00000.00 +00000.00 +183.00 11 0",
				"Pod 3 Point +10780.00 +04018.00 Vector +00000.00 +00000.00 +143.00 11 0", };

		TestUtils.setGameConstantsFromDebug(inputMatchString);

		Player.GameState inputGameState = TestUtils.getGameStateFromDebug(inputGameStateString);
		Player.GameState actualGameState = TestUtils.getGameStateFromDebug(actualGameStateString);
		Player.Action[] myActions = TestUtils.getActionFromDebug(myInputActions);
		Player.Action[] opActions = TestUtils.getActionFromDebug(opInputActions);

		Player.GameState computedGameState = Player.GameEngine.applyAction(inputGameState, myActions, opActions);

		TestUtils.printComputedVSActual(computedGameState, actualGameState);

		assertNotNull(computedGameState);
		assertEquals(actualGameState, computedGameState);

	}

}
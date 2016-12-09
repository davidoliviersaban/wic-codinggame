public class TestUtils {

	// Basically the reverse of method GameState.print()
	public static Player.GameState getGameStateFromDebug(String[] gameStateString) {

		String[] firstLine = gameStateString[0].split(Player.debugSep);
		int round = Integer.valueOf(firstLine[1]);
		int myNbRoundsSinceLastCheckpoint = Integer.valueOf(firstLine[2]);
		int opNbRoundsSinceLastCheckpoint = Integer.valueOf(firstLine[3]);
		int myLaps = Integer.valueOf(firstLine[4]);
		int opLaps = Integer.valueOf(firstLine[5]);

		Player.GameState result = new Player.GameState(round, myNbRoundsSinceLastCheckpoint, opNbRoundsSinceLastCheckpoint, myLaps, opLaps);

		for (int i = 0; i < result.boostUsed.length; i++) {
			result.boostUsed[i] = Boolean.valueOf(firstLine[6 + i]);
		}
		result.isWonGame = Boolean.valueOf(firstLine[10]);
		result.isLostGame = Boolean.valueOf(firstLine[11]);

		for (int i = 1; i < gameStateString.length; i++) {

			String[] line = gameStateString[i].split(Player.debugSep);

			Player.Point p = new Player.Point(Double.valueOf(line[3]), Double.valueOf(line[4]));
			Player.Vector v = new Player.Vector(Double.valueOf(line[6]), Double.valueOf(line[7]));
			double angle = Double.valueOf(line[8]);
			int nextCPId = Integer.valueOf(line[9]);
			int shieldCountDown = Integer.valueOf(line[10]);

			result.pods[i - 1] = new Player.Pod(i - 1, p, v, angle, Player.MatchConstants.checkPoints[nextCPId - 10], shieldCountDown);

		}

		Player.Time.startRoundTimer();
		return result;
	}

	// Basically the reverse of method GameConstants.print()
	public static void setGameConstantsFromDebug(String[] matchString) {

		String[] firstLine = matchString[0].split(Player.debugSep);

		Player.MatchConstants.laps = Integer.valueOf(firstLine[1]);
		Player.MatchConstants.checkPoints = new Player.CheckPoint[(firstLine.length - 2) / 7];

		for (int i = 0; i < Player.MatchConstants.checkPoints.length; i++) {
			Player.MatchConstants.checkPoints[i] = new Player.CheckPoint(i + 10, new Player.Point(Double.valueOf(firstLine[4 + i * 7]), Double.valueOf(firstLine[5 + i * 7])));

		}

	}

	// Basically the reverse of method Actions.print()
	public static Player.Action[] getActionFromDebug(String[] actionsString) {

		Player.Action[] result = new Player.Action[2];

		for (int i = 0; i < result.length; i++) {
			String[] line = actionsString[i].split(Player.debugSep);

			Player.ActionType type = Player.ActionType.valueOf(line[1]);

			Player.Point target = new Player.Point(Double.valueOf(line[3]), Double.valueOf(line[4]));
			int thrust = Integer.valueOf(line[5]);

			switch (type) {
			case MOVE:
				result[i] = Player.Action.getMoveAction(target, thrust);
				break;
			case BOOST:
				result[i] = Player.Action.getBoostAction(target);
				break;
			case SHIELD:
				result[i] = Player.Action.getShieldAction(target);
			default:
				break;
			}

		}

		return result;

	}

	public static void printAction(Player.Action action) {
		System.out.println("Action:   " + action);

	}

	public static void printComputedVSActual(Player.GameState computedGameState, Player.GameState actualGameState) {
		System.err.println("Computed vs Actual:");
		System.err.println(computedGameState);
		System.err.println(actualGameState);
		System.err.println(computedGameState.pods[0]);
		System.err.println(actualGameState.pods[0]);
		System.err.println(computedGameState.pods[1]);
		System.err.println(actualGameState.pods[1]);
		System.err.println(computedGameState.pods[2]);
		System.err.println(actualGameState.pods[2]);
		System.err.println(computedGameState.pods[3]);
		System.err.println(actualGameState.pods[3]);

	}

}

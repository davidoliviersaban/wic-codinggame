import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.Scanner;

class Player {

	// Misc stuff
	public static final boolean isDebugOn = true;

	// Game constants, write them here once for all.
	public static final int minThrust = 0;
	public static final int maxThrust = 100;
	public static final int boostThrust = 1000;
	public static final int W = 16000;
	public static final int H = 9000;
	public static final int checkPointRadius = 600;
	public static final int maxTurnToReachCheckPoint = 100;

	// Game variables
	private static GameState previousGameState;
	private static GameState predictedGameState;
	private static boolean stopGame = false;

	// AIs
	public static StupidAI stupidAI = new StupidAI();
	public static AI ai = stupidAI;

	public static void main(String args[]) {

		Scanner in = new Scanner(System.in);
		initMatch(in);

		// game loop
		while (true) {

			GameState gs = initRound(in);

			Action action = ai.computeAction(gs);

			finalizeRound(action, gs);

			out(action);

		}
	}

	private static void initMatch(Scanner in) {

		debug("Starting the match !");
		ai.printAI();

		Time.startRoundTimer();
		// Memory.initMemory();

	}

	private static GameState initRound(Scanner in) {

		GameState result = null;

		Pod myPod = new Pod(new Point(in.nextInt(), in.nextInt()));
		Time.startRoundTimer();

		PointInt nextCheckPoint = new PointInt(in.nextInt(), in.nextInt());
		myPod.nextCheckpointDist = in.nextInt(); // distance to the next checkpoint
		myPod.nextCheckpointAngle = in.nextInt(); // angle between your pod orientation and the direction of the next checkpoint

		Pod opPod = new Pod(new Point(in.nextInt(), in.nextInt()));

		if (previousGameState == null) {
			result = new GameState(1, myPod, opPod, nextCheckPoint);
		} else {

			result = new GameState(previousGameState.round + 1, myPod, opPod, nextCheckPoint);
		}

		if (isDebugOn) {
			result.print();
		}

		compareInputAgainstPrediction(result);

		return result;

	}

	// Runs a comparison between what CG gives us, and what we had predicted. Will stop the game if any difference is found, in order to highlight the need of a new test
	private static void compareInputAgainstPrediction(GameState gameStateFromInput) {
		if (predictedGameState != null && !predictedGameState.equals(gameStateFromInput)) {
			stopGame = true;
			debug("Ran comparison between the input and the prediction and...");
			debug("Got this:  " + gameStateFromInput);
			debug("Predicted: " + predictedGameState);
			debug("Stop the game");
		} else {
			debug("Prediction ok !");
		}

	}

	private static void finalizeRound(Action action, GameState gs) {

		if (!stopGame) {

			previousGameState = gs;
			predictedGameState = GameEngine.applyAction(gs, action);

			Time.debugRoundDuration();
		}

	}

	public static class Memory {

		// Handles memory stuff, see https://www.codingame.com/forum/t/java-jvm-memory-issues/1494/25
		public static void initMemory() {

			if (isDebugOn) {
				String debugString = getJVMParams();
				debug("JVM params: " + debugString);
				debug("Starting memory before alloc:");
				debugCurrentMemory();
			}

			GameState[] alloc = new GameState[1000000];
			int i = 0;
			long maxMemory = Runtime.getRuntime().maxMemory();

			while (Runtime.getRuntime().totalMemory() < maxMemory * 0.9 && Time.isTimeLeft(true) && i < alloc.length) {
				alloc[i] = new GameState(i);
				i++;
			}

			alloc = null;
			System.gc();

			if (isDebugOn) {
				debug("New memory after alloc:");
				debugCurrentMemory();
			}

		}

		private static String getJVMParams() {
			// Display memory options
			RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
			List<String> arguments = runtimeMxBean.getInputArguments();
			String debugString = "";
			for (String string : arguments) {
				debugString += string + " ";
			}
			return debugString;
		}

		private static void debugCurrentMemory() {
			debug("totalMemory: " + Runtime.getRuntime().totalMemory());
			debug("maxMemory:   " + Runtime.getRuntime().maxMemory());
			debug("freeMemory:  " + Runtime.getRuntime().freeMemory());
		}

	}

	public static class Time {
		// Time constants
		private static final int maxRoundTime = 150; // 150 ms max to answer
		private static final int roundTimeMargin = 1;
		private static final int maxFirstRoundTime = 1000; // 1 s max to answer for first turn only
		private static final int firstRoundTimeMargin = 50;
		public static int maxRoundTimeWithMargin = maxRoundTime - roundTimeMargin;
		public static int maxFirstRoundTimeWithMargin = maxFirstRoundTime - firstRoundTimeMargin;
		public static boolean noTimeLimit = false;

		// Time variables
		private static long roundStartTime;

		public static void startRoundTimer() {
			roundStartTime = System.currentTimeMillis();
		}

		public static boolean isTimeLeft(boolean firstTurn) {
			return getRoundDuration() < maxRoundTimeWithMargin || (firstTurn && getRoundDuration() < maxFirstRoundTimeWithMargin) || noTimeLimit;
		}

		public static boolean isTimeLeft() {
			return isTimeLeft(false);
		}

		private static long getRoundDuration() {
			return System.currentTimeMillis() - roundStartTime;
		}

		public static void debugRoundDuration() {
			debug("Round duration: " + getRoundDuration());

		}

	}

	private static void out(Action action) {
		if (stopGame) {
			System.out.println("Failure!");
		} else {
			if (action.thrust == boostThrust) {
				System.out.println(action.target.getX() + " " + action.target.getY() + " BOOST");
			} else {
				System.out.println(action.target.getX() + " " + action.target.getY() + " " + action.thrust);
			}

		}

	}

	private static void debug(String message) {
		if (isDebugOn) {
			System.err.println(message);
		}
	}

	private static void debugForced(String message) {
		System.err.println(message);
	}

	private static final String debugStartLine = "\"";
	private static final String debugEndLine = "\",";
	public static final String debugSep = " ";

	// Debug for later input in tests
	public static void debugForInput(String message) {
		debug(debugStartLine + message + debugEndLine);
	}

	public static class Action {
		public PointInt target;
		public int thrust;

		public Action(PointInt target, int thrust) {
			super();
			this.target = target;
			this.thrust = thrust;
		}

	}

	// Stores stuff which is not going to change for the whole match, but could change from one match to another
	public static class MatchConstants {

		public static void print() {

			debugForInput("");
		}

	}

	public static class GameEngine {

		public static int nbApplyAction = 0;

		public static GameState applyAction(GameState gs, Action action) {

			// TODO: implement the game engine, which applies to a given GameState the provided action, and returns the new action.
			// This is basically reimplementing what CG did on their side.
			// This is NOT mandatory to do it, but most of the time (if not all), it's necessary to know exactly what we're going to get if we apply a given action
			// All AIs based on simulations will need this
			// Note: the returned GameState could be a copy or not of the provided one

			nbApplyAction++;
			return null;
		}

	}

	public static class GameStateObject {

		public void print() {
			debugForInput(toString());
		}

	}

	public static class Point extends GameStateObject {
		public double x;
		public double y;

		public Point(double x, double y) {
			super();
			this.x = x;
			this.y = y;
		}

		public Point addVector(Vector v) {
			this.x += v.x;
			this.y += v.y;
			return this;
		}

		public Point copy() {
			return new Point(x, y);
		}

		@Override
		public String toString() {
			return "Point " + String.format("%3.5f", x) + " " + String.format("%3.5f", y);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(x);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(y);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Point other = (Point) obj;
			if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
				return false;
			if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
				return false;
			return true;
		}

		public boolean equalsRounded(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Point other = (Point) obj;
			if (Math.round(x) != Math.round(other.x))
				return false;
			if (Math.round(y) != Math.round(other.y))
				return false;
			return true;
		}

	}

	public static class PointInt extends Point {

		public PointInt(int xInt, int yInt) {
			super(xInt, yInt);
		}

		public int getX() {
			return (int) x;
		}

		public int getY() {
			return (int) y;
		}

		public PointInt copy() {
			return new PointInt(getX(), getY());
		}

	}

	public static class Vector extends GameStateObject {
		public double x;
		public double y;
		private double norm;
		private double normSquare;

		public Vector(double x, double y) {
			super();
			this.x = x;
			this.y = y;
		}

		public Vector(Point p1, Point p2) {
			this(p2.x - p1.x, p2.y - p1.y);
		}

		public Vector(double angleDegree, double norm, int ignore) {
			this(norm * Math.cos(Math.toRadians(angleDegree)), norm * Math.sin(Math.toRadians(angleDegree)));
		}

		public double getNorm() {
			if (norm == 0) {
				norm = Math.sqrt(x * x + y * y);
			}
			return norm;
		}

		public double getNormSquare() {
			if (normSquare == 0) {
				normSquare = x * x + y * y;
			}
			return normSquare;
		}

		public Vector add(Vector v) {
			this.x += v.x;
			this.y += v.y;
			resetNorm();
			return this;
		}

		public Vector mul(double d) {
			this.x *= d;
			this.y *= d;
			resetNorm();
			return this;
		}

		private void resetNorm() {
			this.norm = 0;
			this.normSquare = 0;
		}

		public Vector copy() {
			return new Vector(x, y);
		}

		@Override
		public String toString() {
			return "Vector " + String.format("%3.5f", x) + " " + String.format("%3.5f", y);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(x);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(y);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Vector other = (Vector) obj;
			if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
				return false;
			if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
				return false;
			return true;
		}

		public boolean equalsRounded(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Vector other = (Vector) obj;
			if (Math.round(x) != Math.round(other.x))
				return false;
			if (Math.round(y) != Math.round(other.y))
				return false;
			return true;
		}

	}

	public static Vector addVectors(Vector v1, Vector v2) {
		return new Vector(v1.x + v2.x, v1.y + v2.y);
	}

	public static Point addVectorToPoint(Point p, Vector v) {
		return new Point(p.x + v.x, p.y + v.y);
	}

	public static double getDistance(Point p1, Point p2) {
		return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
	}

	public static double getDistanceSquare(Point p1, Point p2) {
		return (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y);
	}

	public static class GameState extends GameStateObject {
		public int round;

		public Pod myPod;
		public Pod opPod;

		public PointInt nextCheckPoint;

		public GameState(int round) {
			this.round = round;
		}

		public GameState(int round, Pod myPod, Pod opPod, PointInt nextCheckPoint) {
			super();
			this.round = round;
			this.myPod = myPod;
			this.opPod = opPod;
			this.nextCheckPoint = nextCheckPoint;
		}

		public GameState copy() {
			return new GameState(round, new Pod(myPod.p.copy()), new Pod(opPod.p.copy()), nextCheckPoint.copy());
		}

		@Override
		public String toString() {
			return "GameState" + debugSep + round + debugSep + "MyPod:" + debugSep + myPod + debugSep + "OpPod:" + debugSep + opPod + debugSep + "CP:" + debugSep + nextCheckPoint;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((myPod == null) ? 0 : myPod.hashCode());
			result = prime * result + ((nextCheckPoint == null) ? 0 : nextCheckPoint.hashCode());
			result = prime * result + ((opPod == null) ? 0 : opPod.hashCode());
			result = prime * result + round;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			GameState other = (GameState) obj;
			if (myPod == null) {
				if (other.myPod != null)
					return false;
			} else if (!myPod.equals(other.myPod))
				return false;
			if (nextCheckPoint == null) {
				if (other.nextCheckPoint != null)
					return false;
			} else if (!nextCheckPoint.equals(other.nextCheckPoint))
				return false;
			if (opPod == null) {
				if (other.opPod != null)
					return false;
			} else if (!opPod.equals(other.opPod))
				return false;
			if (round != other.round)
				return false;
			return true;
		}

	}

	public static class Pod extends GameStateObject {

		public Point p;
		public int nextCheckpointDist;
		public int nextCheckpointAngle;

		public Pod(Point p) {
			super();
			this.p = p;
		}

		@Override
		public String toString() {
			return "Pod " + p + " " + nextCheckpointDist + " " + nextCheckpointAngle;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + nextCheckpointAngle;
			result = prime * result + nextCheckpointDist;
			result = prime * result + ((p == null) ? 0 : p.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Pod other = (Pod) obj;
			if (nextCheckpointAngle != other.nextCheckpointAngle)
				return false;
			if (nextCheckpointDist != other.nextCheckpointDist)
				return false;
			if (p == null) {
				if (other.p != null)
					return false;
			} else if (!p.equals(other.p))
				return false;
			return true;
		}

	}

	public static abstract class AI {

		public abstract Action compute(GameState gs);

		public void printAIParameters() {
			// Nothing to print by default
		}

		public Action computeAction(GameState gs) {
			printAIParameters();
			return compute(gs);
		}

		public void printAI() {
			debug("Using base AI: " + this.getClass().getName());
		}
	}

	public static class StupidAI extends AI {

		@Override
		public Action compute(GameState gs) {

			Action result = null;

			if (gs.myPod.nextCheckpointAngle > 90 || gs.myPod.nextCheckpointAngle < -90) {
				result = new Action(gs.nextCheckPoint, minThrust);
			} else {
				result = new Action(gs.nextCheckPoint, boostThrust);
			}

			return result;
		}

	}

}
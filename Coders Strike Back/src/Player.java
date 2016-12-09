import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

class Player {

	// Misc stuff
	public static boolean isDebugOn = true;
	public static final boolean isCompareFailureOn = true;

	// Game constants, write them here once for all.
	private static final int minThrust = 0;
	private static final int maxThrust = 100;
	private static final int boostThrust = 650;
	private static final int podRadius = 400;
	private static final int checkPointRadius = 600;
	private static final int maxTurnToReachCheckPoint = 100;
	private static final int maxAngle = 18;
	private static final int minImpulse = 120;
	private static final int shieldWeight = 10;
	private static final double podFriction = 0.85;

	// Game variables
	private static GameState previousGameState;
	private static GameState predictedGameState;
	private static boolean stopGame = false;

	// AIs
	public static StupidAI stupidAI = new StupidAI(false);
	public static StupidAI stupidAIOp = new StupidAI(true);
	public static JahzNoobAI jazhNoobAI = new JahzNoobAI();
	public static AI ai = stupidAI; // The one which is used

	public static void main(String args[]) {

		Scanner in = new Scanner(System.in);
		initMatch(in);

		// game loop
		while (true) {

			GameState gs = initRound(in);

			Action[] actions = ai.computeAction(gs);

			finalizeRound(actions, gs);

			out(actions);

		}
	}

	private static void initMatch(Scanner in) {

		debug("Starting the match !");
		ai.printAI();

		MatchConstants.laps = in.nextInt();

		Time.startRoundTimer();

		MatchConstants.checkPoints = new CheckPoint[in.nextInt()];
		for (int i = 0; i < MatchConstants.checkPoints.length; i++) {
			MatchConstants.checkPoints[i] = new CheckPoint(i + 10, new Point(in.nextInt(), in.nextInt()));

		}

		if (isDebugOn) {
			MatchConstants.print();
		}

		// Memory.initMemory();

	}

	private static GameState initRound(Scanner in) {

		GameState result = null;

		if (previousGameState == null) {
			result = new GameState(1, 0, 0, 0, 0);
		} else {
			result = new GameState(previousGameState.round + 1, predictedGameState.myNbRoundsSinceLastCheckpoint, predictedGameState.opNbRoundsSinceLastCheckpoint, predictedGameState.myLaps,
					predictedGameState.opLaps);
			// TODO: evaluate more carefully myLaps and myNbRoundsSinceLastCheckpoint based on nextCheckPoint (we can't be sure of what we had predicted since we don't know what the op is doing)
			// TODO: evaluate more carefully opLaps and opNbRoundsSinceLastCheckpoint, based on where the op Pods are
		}

		for (int i = 0; i < 2; i++) {
			int x = in.nextInt();
			int shieldCountDown = 0;
			if (previousGameState != null) {
				if (i == 0) {
					Time.startRoundTimer();
				}
				shieldCountDown = Math.max(0, previousGameState.pods[i].shieldCountDown - 1);

			}

			result.pods[i] = new Pod(i, new Point(x, in.nextInt()), new Vector(in.nextInt(), in.nextInt()), in.nextInt(), MatchConstants.checkPoints[in.nextInt()], shieldCountDown);

			if (predictedGameState != null) {
				result.boostUsed[i] = predictedGameState.boostUsed[i]; // We can be sure of our prediction since it doesn't depend on the op
			}

		}
		for (int i = 0; i < 2; i++) {
			int shieldCountDown = 0;
			if (previousGameState != null) {
				shieldCountDown = Math.max(0, previousGameState.pods[i + 2].shieldCountDown - 1);
			}

			result.pods[i + 2] = new Pod(i + 2, new Point(in.nextInt(), in.nextInt()), new Vector(in.nextInt(), in.nextInt()), in.nextInt(), MatchConstants.checkPoints[in.nextInt()], shieldCountDown);

			if (predictedGameState != null) {
				result.boostUsed[i + 2] = predictedGameState.boostUsed[i + 2]; // TODO: check the pod acceleration to detect a boost
			}

		}

		if (isDebugOn) {
			result.print();
		}

		compareInputAgainstPrediction(result);

		return result;

	}

	// Runs a comparison between what CG gives us, and what we had predicted. May stop the game if any difference is found, in order to highlight the need of a new test
	private static void compareInputAgainstPrediction(GameState gameStateFromInput) {

		if (predictedGameState != null && !gameStateFromInput.equals(predictedGameState)) {

			debug("Ran comparison between the input and the prediction and predicted:");
			predictedGameState.print();

			if (isCompareFailureOn) {
				stopGame = true;
				debug("Stop the game");
			} else {
				debug("Should have stopped the game");
			}

		} else {
			debug("Prediction ok !");
		}

	}

	private static void finalizeRound(Action[] myActions, GameState gs) {

		if (!stopGame) {

			previousGameState = gs;

			Action[] opActions = stupidAIOp.computeAction(gs);

			debug("Actions:");
			myActions[0].print();
			myActions[1].print();
			opActions[0].print();
			opActions[1].print();

			predictedGameState = GameEngine.applyActionWithCopy(gs, myActions, opActions);

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

	private static void out(Action[] actions) {
		if (stopGame) {
			System.out.println("Failure!");
		} else {
			for (int i = 0; i < actions.length; i++) {

				switch (actions[i].type) {
				case MOVE:
					System.out.println(((int) actions[i].target.x) + " " + ((int) actions[i].target.y) + " " + actions[i].thrust);
					break;
				case BOOST:
				case SHIELD:
					System.out.println(((int) actions[i].target.x) + " " + ((int) actions[i].target.y) + " " + actions[i].type);
					break;
				default:
					break;
				}

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

	public static enum ActionType {
		MOVE, BOOST, SHIELD
	}

	public static class Action extends GameStateObject {

		public ActionType type;
		public Point target;
		public int thrust;

		private Action(ActionType type, Point target, int thrust) {
			super();
			this.type = type;
			this.target = target;
			this.thrust = thrust;
		}

		@Override
		public String toString() {
			return "Action " + type + " " + target + " " + thrust;
		}

		public static Action getMoveAction(Point target, int thrust) {
			return new Action(ActionType.MOVE, target, thrust);
		}

		public static Action getBoostAction(Point target) {
			return new Action(ActionType.BOOST, target, boostThrust);
		}

		public static Action getShieldAction(Point target) {
			return new Action(ActionType.SHIELD, target, minThrust);
		}

	}

	// Stores stuff which is not going to change for the whole match, but could change from one match to another
	public static class MatchConstants {

		public static int laps;
		public static CheckPoint[] checkPoints;

		public static void print() {

			String s = "MatchConstants: " + laps + " ";
			for (CheckPoint checkPoint : checkPoints) {
				s += checkPoint + debugSep;
			}

			debugForInput(s);
		}

	}

	public static class GameEngine {

		public static int nbApplyAction = 0;

		// Apply the actions of the players on a given gamestate, and return the new one as if computed by CG (or almost...). Performs a copy, so the provided gs will stay untouched.
		public static GameState applyActionWithCopy(GameState gs, Action[] myActions, Action[] opActions) {
			GameState result = gs.copy();
			return applyActionWithoutCopy(result, myActions, opActions);
		}

		// Apply the actions of the players on a given gamestate, and return the new one as if computed by CG (or almost...). Does NOT perform a copy, so the provided gamestate will be modified
		public static GameState applyActionWithoutCopy(GameState result, Action[] myActions, Action[] opActions) {

			if (!result.isWonGame && !result.isLostGame) {
				// If the game is not finished, apply the engine, otherwise there is nothing to do
				setShields(myActions, opActions, result);

				boostPods(myActions, opActions, result);

				play(result);

				finalizeAll(result);

				result.round++;
				result.myNbRoundsSinceLastCheckpoint++;
				result.opNbRoundsSinceLastCheckpoint++;

				if (result.myNbRoundsSinceLastCheckpoint == maxTurnToReachCheckPoint) {
					debug("I lose due to timeout :(");
					result.isLostGame = true;
				} else if (result.opNbRoundsSinceLastCheckpoint == maxTurnToReachCheckPoint) {
					debug("I win due to timeout :)");
					result.isWonGame = true;
				}

				nbApplyAction++;
			}

			return result;
		}

		// If a pod used a shield, update the shieldCountDown
		private static void setShields(Action[] myActions, Action[] opActions, GameState result) {
			for (int i = 0; i < myActions.length; i++) {
				Action action = myActions[i];
				if (action.type == ActionType.SHIELD) {
					result.pods[i].shieldCountDown = 3;
				}
			}

			for (int i = 0; i < opActions.length; i++) {
				Action action = opActions[i];
				if (action.type == ActionType.SHIELD) {
					result.pods[i + 2].shieldCountDown = 3;
				}
			}
		}

		// Boost each of the pod
		private static void boostPods(Action[] myActions, Action[] opActions, GameState result) {

			for (int i = 0; i < 4; i++) {

				Pod pod = result.pods[i];
				Action action = null;

				if (i < 2) {
					action = myActions[i];

				} else {
					action = opActions[i - 2];
				}

				if (action.type == ActionType.BOOST) {
					if (result.boostUsed[i]) {
						// BOOST was already used in the game ! So get back to maxThrust
						action.thrust = maxThrust;
					}
					// Now in any case it's used !
					result.boostUsed[i] = true;
				}

				pod.boost(action.target, action.thrust, result.round == 1);

			}

		}

		private static void play(GameState gs) {

			// This tracks the time during the turn. The goal is to reach 1.0
			double elapsedTime = 0.0;

			List<Collision> previousCollisions = new ArrayList<>();

			while (elapsedTime < 1.0) {

				debug("ElapsedTime: " + elapsedTime);

				EntitiesCollision firstCollision = null;

				// We look for all the collisions that are going to occur during the turn
				for (int i = 0; i < gs.pods.length; ++i) {

					Pod pod1 = gs.pods[i];

					// Collision with another pod ?
					for (int j = i + 1; j < gs.pods.length; ++j) {

						Pod pod2 = gs.pods[j];

						EntitiesCollision col = getEntitiesCollision(pod1, pod2);

						// If the collision occurs earlier than the one we currently have we keep it
						if (col != null && col.time + elapsedTime < 1.0 && (firstCollision == null || col.time < firstCollision.time) && !previousCollisions.contains(col)) {
							firstCollision = col;
						}
					}

					// Collision with the next checkpoint ?
					EntitiesCollision col = getEntitiesCollision(pod1, pod1.nextCheckPoint);

					// If the collision occurs earlier than the one we currently have we keep it
					if (col != null && col.time + elapsedTime < 1.0 && (firstCollision == null || col.time < firstCollision.time) && !previousCollisions.contains(col)) {
						firstCollision = col;
					}

				}

				if (firstCollision == null) {

					debug("No collision !");

					// No collision, we can move the pods until the end of the turn
					moveAll(gs, 1 - elapsedTime);
					// End of the turn
					elapsedTime = 1.0;

				} else {

					debug("Collision ! " + firstCollision);

					if (firstCollision.time > 0) {
						// We're now in a new "round" of collisions, so clear the list
						previousCollisions.clear();
					}

					// Move the pods to reach the time `t` of the collision
					moveAll(gs, firstCollision.time);

					// Play out the collision
					bounceEntities(firstCollision.e1, firstCollision.e2, gs);

					elapsedTime += firstCollision.time;
					previousCollisions.add(firstCollision);
				}
			}

		}

		private static EntitiesCollision getEntitiesCollision(Entity e1, Entity e2) {

			// Square of the distance
			double dist = getDistanceSquare(e1.p, e2.p);

			// Sum of the radii squared
			double sr = 0;

			if (e1 instanceof CheckPoint || e2 instanceof CheckPoint) {
				sr = 360000;
			} else {
				sr = (e1.getRadius() + e2.getRadius()) * (e1.getRadius() + e2.getRadius());
			}

			if (dist < sr) {
				// Objects are already touching each other. We have an immediate collision.
				return new EntitiesCollision(e1, e2, 0);
			}

			// Optimisation. Objects with the same speed will never collide
			if (e1.speed.equals(e2.speed)) {
				return null;
			}

			// We place ourselves in the reference frame of e2. e2 is therefore stationary and is at (0,0)

			Point e1p = new Point(e1.p.x - e2.p.x, e1.p.y - e2.p.y);
			Vector e1v = new Vector(e1.speed.x - e2.speed.x, e1.speed.y - e2.speed.y);
			Point e2p = new Point(0, 0);

			// We look for the closest point to e2 (which is in (0,0)) on the line described by our speed vector
			Point pClosest = getClosestPoint(e2p, e1p, addVectorToPoint(e1p, e1v));

			// Square of the distance between e2 and the closest point to e2 on the line described by our speed vector
			double pdist = getDistanceSquare(e2p, pClosest);

			// Square of the distance between e1p and that point
			double mypdist = getDistanceSquare(e1p, pClosest);

			// If the distance between e2 and this line is less than the sum of the radii, there might be a collision
			if (pdist < sr) {
				// Our speed on the line
				double length = e1v.getNorm();

				// We move along the line to find the point of impact
				double backdist = Math.sqrt(sr - pdist);
				pClosest.x = pClosest.x - backdist * (e1v.x / length);
				pClosest.y = pClosest.y - backdist * (e1v.y / length);
				double newdist = getDistanceSquare(e1p, pClosest);

				// If the point is now further away it means we are not going the right way, therefore the collision won't happen
				if (newdist > mypdist) {
					return null;
				}

				pdist = getDistance(pClosest, e1p);

				// The point of impact is further than what we can travel in one turn
				if (pdist > length) {
					return null;
				}

				// Time needed to reach the impact point
				double t = pdist / length;

				return new EntitiesCollision(e1, e2, t);
			}

			return null;
		}

		private static void bounceEntities(Entity e1, Entity e2, GameState gs) {

			boolean e1CheckPoint = e1 instanceof CheckPoint;
			boolean e2CheckPoint = e2 instanceof CheckPoint;

			if (e1CheckPoint) {
				((Pod) e2).nextCheckPoint = MatchConstants.checkPoints[(((Pod) e2).nextCheckPoint.id + 1 - 10) % MatchConstants.checkPoints.length];
				if (((Pod) e2).id < 2) {
					gs.myNbRoundsSinceLastCheckpoint = -1;
					if (((Pod) e2).nextCheckPoint.id == 1) {
						gs.myLaps++;
						if (gs.myLaps == MatchConstants.laps) {
							gs.isWonGame = true;
							debug("I'll win it !");
						}
					}
				} else {
					gs.opNbRoundsSinceLastCheckpoint = -1;
					if (((Pod) e2).nextCheckPoint.id == 1) {
						gs.opLaps++;
						if (gs.opLaps == MatchConstants.laps) {
							gs.isLostGame = true;
							debug("I'll loose it !");
						}
					}
				}
			} else if (e2CheckPoint) {
				((Pod) e1).nextCheckPoint = MatchConstants.checkPoints[(((Pod) e1).nextCheckPoint.id + 1 - 10) % MatchConstants.checkPoints.length];
				if (((Pod) e1).id < 2) {
					gs.myNbRoundsSinceLastCheckpoint = -1;
					if (((Pod) e1).nextCheckPoint.id == 1) {
						gs.myLaps++;
						if (gs.myLaps == MatchConstants.laps) {
							gs.isWonGame = true;
							debug("I'll win it !");
						}
					}
				} else {
					gs.opNbRoundsSinceLastCheckpoint = -1;
					if (((Pod) e1).nextCheckPoint.id == 1) {
						gs.opLaps++;
						if (gs.opLaps == MatchConstants.laps) {
							gs.isLostGame = true;
							debug("I'll loose it !");
						}
					}
				}
			} else {

				// If a pod has its shield active its mass is 10 otherwise it's 1
				double m1 = ((Pod) e1).getWeight();
				double m2 = ((Pod) e2).getWeight();
				double mcoeff = (m1 + m2) / (m1 * m2);

				double nx = e1.p.x - e2.p.x;
				double ny = e1.p.y - e2.p.y;

				// Square of the distance between the 2 pods. This value could be hardcoded because it is always 800²
				// double nxnysquare = nx * nx + ny * ny;
				double nxnysquare = 640000;

				double dvx = e1.speed.x - e2.speed.x;
				double dvy = e1.speed.y - e2.speed.y;

				// fx and fy are the components of the impact vector. product is just there for optimisation purposes
				double product = nx * dvx + ny * dvy;
				double fx = (nx * product) / (nxnysquare * mcoeff);
				double fy = (ny * product) / (nxnysquare * mcoeff);

				// We apply the impact vector once
				e1.speed.x -= fx / m1;
				e1.speed.y -= fy / m1;
				e2.speed.x += fx / m2;
				e2.speed.y += fy / m2;

				// If the norm of the impact vector is less than 100, we normalize it to 100
				double impulse = Math.sqrt(fx * fx + fy * fy);
				if (impulse < minImpulse) {
					fx = fx * minImpulse / impulse;
					fy = fy * minImpulse / impulse;
				}

				// We apply the impact vector a second time
				e1.speed.x -= fx / m1;
				e1.speed.y -= fy / m1;
				e2.speed.x += fx / m2;
				e2.speed.y += fy / m2;

			}

		}

		// Get the closest point from p on the line a-b
		private static Point getClosestPoint(Point p, Point a, Point b) {

			double da = b.y - a.y;
			double db = a.x - b.x;
			double c1 = da * a.x + db * a.y;
			double c2 = -db * p.x + da * p.y;
			double det = da * da + db * db;
			double cx = 0;
			double cy = 0;

			if (det != 0) {
				cx = (da * c1 - db * c2) / det;
				cy = (da * c2 + db * c1) / det;
			} else {
				// The point is already on the line
				cx = p.x;
				cy = p.y;
			}

			return new Point(cx, cy);

		}

		private static void moveAll(GameState result, double time) {

			for (Pod pod : result.pods) {
				pod.move(time);
			}

		}

		private static void finalizeAll(GameState result) {

			for (Pod pod : result.pods) {
				pod.finalizeMove();
			}

		}

	}

	public static class GameStateObject {

		public void print() {
			debugForInput(toString());
		}

	}

	private static final DecimalFormat pointVectorformatter = new DecimalFormat("+#00000.00;-#");

	public static class Point extends GameStateObject {
		public double x;
		public double y;

		public Point(double x, double y) {
			super();
			this.x = x;
			this.y = y;
		}

		public void round() {
			x = Math.round(x);
			y = Math.round(y);
		}

		public double distance2(Point p) {
			return getDistanceSquare(this, p);
		}

		public double distance(Point p) {
			return getDistance(this, p);
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
			return "Point " + pointVectorformatter.format(x) + " " + pointVectorformatter.format(y);
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
			if (Math.abs(x - other.x) > 1)
				return false;
			if (Math.abs(y - other.y) > 1)
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

		public boolean equalsAt1(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Point other = (Point) obj;
			if (Math.abs(x - other.x) > 1)
				return false;
			if (Math.abs(y - other.y) > 1)
				return false;
			return true;
		}

	}

	public static class Vector extends GameStateObject {
		public double x;
		public double y;
		private double norm;
		private double normSquare;
		private double angle;

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

		public void round() {
			x = Math.round(x);
			y = Math.round(y);
		}

		public void truncate() {
			x = (int) x;
			y = (int) y;
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

		// Returns the angle in degree between 0 and 360
		public double getAngle() {
			if (angle == 0) {
				if (y < 0) {
					angle = 360 - Math.toDegrees(Math.acos(x / getNorm()));
				} else {
					angle = Math.toDegrees(Math.acos(x / getNorm()));
				}

			}
			return angle;
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
			return "Vector " + pointVectorformatter.format(x) + " " + pointVectorformatter.format(y);
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

		public boolean equalsAt1(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Vector other = (Vector) obj;
			if (Math.abs(x - other.x) > 1)
				return false;
			if (Math.abs(y - other.y) > 1)
				return false;
			return true;
		}

	}

	public static Vector addVectors(Vector v1, Vector v2) {
		return new Vector(v1.x + v2.x, v1.y + v2.y);
	}

	public static Vector mulVector(Vector v1, double d) {
		return new Vector(v1.x * d, v1.y * d);
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

	// Result in degrees, minimal angle between -180 and 180
	public static double getAngleBetweenVectorAndAngle(double angle, Vector v) {

		double angleV = v.getAngle();

		// To know whether we should turn clockwise or not we look at the two ways and keep the smallest
		// The ternary operators replace the use of a modulo operator which would be slower
		double right = angle <= angleV ? angleV - angle : 360.0 - angle + angleV;
		double left = angle >= angleV ? angle - angleV : angle + 360.0 - angleV;

		if (right < left) {
			return right;
		} else {
			// We return a negative angle if we must rotate to left
			return -left;
		}

	}

	public abstract static class Collision {

		public Entity e1;
		public double time;

		public Collision(Entity e1, double time) {
			super();
			this.e1 = e1;
			this.time = time;
		}

	}

	public static class EntitiesCollision extends Collision {

		public Entity e2;

		public EntitiesCollision(Entity e1, Entity e2, double time) {
			super(e1, time);
			this.e2 = e2;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((e1 == null) ? 0 : e1.hashCode());
			result = prime * result + ((e2 == null) ? 0 : e2.hashCode());
			long temp;
			temp = Double.doubleToLongBits(time);
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
			EntitiesCollision other = (EntitiesCollision) obj;
			if (e1 == null) {
				if (other.e1 != null)
					return false;
			} else if (!e1.equals(other.e1))
				return false;
			if (e2 == null) {
				if (other.e2 != null)
					return false;
			} else if (e2.id != other.e2.id)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Entities collision " + e1.id + " " + e2.id + " " + time;
		}

	}

	public abstract static class Entity extends GameStateObject {

		public int id;
		public Point p;
		public Vector speed;

		public Entity(int id, Point p, Vector speed) {
			super();
			this.id = id;
			this.p = p;
			this.speed = speed;
		}

		public abstract double getRadius();

		public abstract double getWeight();

		public abstract double getFriction();

		public void move(double time) {
			Vector move = mulVector(speed, time);
			p = addVectorToPoint(p, move);

		}

		public void finalizeMove() {
			speed = speed.mul(getFriction());
			p.round();
			speed.truncate();
		}

		public abstract Entity copy();

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + id;
			result = prime * result + ((p == null) ? 0 : p.hashCode());
			result = prime * result + ((speed == null) ? 0 : speed.hashCode());
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
			Entity other = (Entity) obj;
			if (id != other.id)
				return false;
			if (p == null) {
				if (other.p != null)
					return false;
			} else if (!p.equalsAt1(other.p))
				return false;
			if (speed == null) {
				if (other.speed != null)
					return false;
			} else if (!speed.equalsAt1(other.speed))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return String.format("%1d", id) + " " + p + " " + speed;
		}

	}

	public static class CheckPoint extends Entity {

		public CheckPoint(int id, Point p) {
			super(id, p, new Vector(0, 0));
		}

		@Override
		public double getRadius() {
			return checkPointRadius;
		}

		@Override
		public double getWeight() {
			return 0;
		}

		@Override
		public double getFriction() {
			return 0;
		}

		@Override
		public Entity copy() {
			return new CheckPoint(id, p.copy());
		}

	}

	public static class Pod extends Entity {

		public double angle;
		public CheckPoint nextCheckPoint;
		public int shieldCountDown;

		public Pod(int id, Point p, Vector speed, double angle, CheckPoint nextCheckPoint, int shieldCountDown) {
			super(id, p, speed);
			this.angle = angle;
			this.nextCheckPoint = nextCheckPoint;
			this.shieldCountDown = shieldCountDown;
		}

		public void boost(Point target, int thrust, boolean isFirstTurn) {

			if (target != null && !this.p.equals(target)) {

				Vector acceleration = new Vector(this.p, target);

				if (!isFirstTurn && getAngleBetweenVectorAndAngle(angle, acceleration) > maxAngle) {
					angle = angle + 18;
					acceleration = new Vector(angle, thrust, 0);
				} else if (!isFirstTurn && getAngleBetweenVectorAndAngle(angle, acceleration) < -maxAngle) {
					angle = angle - 18;
					acceleration = new Vector(angle, thrust, 0);
				} else {
					angle = acceleration.getAngle();
					acceleration.mul(thrust / acceleration.getNorm());
				}

				this.speed.add(acceleration);
			}

		}

		@Override
		public void finalizeMove() {
			super.finalizeMove();
			angle = Math.round(angle);
		}

		@Override
		public double getRadius() {
			return podRadius;
		}

		@Override
		public double getWeight() {
			if (shieldCountDown > 0) {
				return shieldWeight;
			} else {
				return 1;
			}
		}

		@Override
		public double getFriction() {
			return podFriction;
		}

		@Override
		public Entity copy() {
			return new Pod(id, p.copy(), speed.copy(), angle, nextCheckPoint, shieldCountDown);
		}

		private static final DecimalFormat angleformatter = new DecimalFormat("+#000.00;-#");

		@Override
		public String toString() {
			return "Pod " + id + " " + p + " " + speed + " " + angleformatter.format(angle) + " " + nextCheckPoint.id + " " + shieldCountDown;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			long temp;
			temp = Double.doubleToLongBits(angle);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result + ((nextCheckPoint == null) ? 0 : nextCheckPoint.hashCode());
			result = prime * result + shieldCountDown;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			Pod other = (Pod) obj;
			if (Math.abs((angle - other.angle) % 360) > 1)
				return false;
			if (nextCheckPoint == null) {
				if (other.nextCheckPoint != null)
					return false;
			} else if (!nextCheckPoint.equals(other.nextCheckPoint))
				return false;
			if (shieldCountDown != other.shieldCountDown)
				return false;
			return true;
		}

	}

	public static class GameState extends GameStateObject {

		public int round;
		public int myNbRoundsSinceLastCheckpoint;
		public int opNbRoundsSinceLastCheckpoint;
		public int myLaps;
		public int opLaps;
		public boolean[] boostUsed;

		public Pod[] pods;

		boolean isWonGame;
		boolean isLostGame;

		public GameState(int round) {
			this.round = round;
		}

		public GameState(int round, int myNbRoundsSinceLastCheckpoint, int opNbRoundsSinceLastCheckpoint, int myLaps, int opLaps) {
			super();
			this.round = round;
			this.myNbRoundsSinceLastCheckpoint = myNbRoundsSinceLastCheckpoint;
			this.opNbRoundsSinceLastCheckpoint = opNbRoundsSinceLastCheckpoint;
			this.myLaps = myLaps;
			this.opLaps = opLaps;
			boostUsed = new boolean[4];
			pods = new Pod[4];
			isWonGame = false;
			isLostGame = false;
		}

		public GameState copy() {
			GameState result = new GameState(round, myNbRoundsSinceLastCheckpoint, opNbRoundsSinceLastCheckpoint, myLaps, opLaps);
			result.boostUsed = Arrays.copyOf(boostUsed, 4);
			for (int i = 0; i < pods.length; i++) {
				result.pods[i] = (Pod) pods[i].copy();
			}
			return result;
		}

		@Override
		public String toString() {
			return "GameState " + round + " " + myNbRoundsSinceLastCheckpoint + " " + opNbRoundsSinceLastCheckpoint + " " + myLaps + " " + opLaps + " " + boostUsed[0] + " " + boostUsed[1] + " "
					+ boostUsed[2] + " " + boostUsed[3] + " " + isWonGame + " " + isLostGame;
		}

		@Override
		public void print() {
			debugForInput(toString());
			for (Pod pod : pods) {
				pod.print();
			}

		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(boostUsed);
			result = prime * result + (isLostGame ? 1231 : 1237);
			result = prime * result + (isWonGame ? 1231 : 1237);
			result = prime * result + myLaps;
			result = prime * result + myNbRoundsSinceLastCheckpoint;
			result = prime * result + opLaps;
			result = prime * result + opNbRoundsSinceLastCheckpoint;
			result = prime * result + Arrays.hashCode(pods);
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
			if (!Arrays.equals(boostUsed, other.boostUsed))
				return false;
			if (isLostGame != other.isLostGame)
				return false;
			if (isWonGame != other.isWonGame)
				return false;
			if (myLaps != other.myLaps)
				return false;
			if (myNbRoundsSinceLastCheckpoint != other.myNbRoundsSinceLastCheckpoint)
				return false;
			if (opLaps != other.opLaps)
				return false;
			if (opNbRoundsSinceLastCheckpoint != other.opNbRoundsSinceLastCheckpoint)
				return false;
			if (!Arrays.equals(pods, other.pods))
				return false;
			if (round != other.round)
				return false;
			return true;
		}

	}

	public static abstract class AI {

		public abstract Action[] compute(GameState gs);

		public void printAIParameters() {
			// Nothing to print by default
		}

		public Action[] computeAction(GameState gs) {
			printAIParameters();
			return compute(gs);
		}

		public void printAI() {
			debug("Using base AI: " + this.getClass().getName());
		}
	}

	public static class JahzNoobAI extends AI {

		@Override
		public Action[] compute(GameState gs) {
			Action[] result = new Action[2];
			result[0] = Action.getMoveAction(gs.pods[2].nextCheckPoint.p, 1);
			result[1] = Action.getMoveAction(gs.pods[3].nextCheckPoint.p, 1);
			return result;
		}

	}

	public static class StupidAI extends AI {

		public boolean simulateOp = false;

		public StupidAI(boolean simulateOp) {
			super();
			this.simulateOp = simulateOp;
		}

		@Override
		public Action[] compute(GameState gs) {

			Action[] result = new Action[2];

			int startPodId = 0;
			if (simulateOp) {
				startPodId = 2;
			}

			if (gs.round == 1) {
				result[0] = Action.getBoostAction(gs.pods[startPodId].nextCheckPoint.p);
				result[1] = Action.getBoostAction(gs.pods[startPodId + 1].nextCheckPoint.p);
			} else {

				for (int i = 0; i < 2; i++) {
					Pod pod = gs.pods[startPodId + i];

					Point target = addVectorToPoint(pod.nextCheckPoint.p, mulVector(pod.speed, -1));

					double angle = getAngleBetweenVectorAndAngle(pod.angle, new Vector(pod.p, target));

					if (angle > 90) {
						// We're not in the good direction, slow down
						result[i] = Action.getMoveAction(target, minThrust);
					} else {
						// We're well aligned
						double d = getDistance(pod.p, target);

						if (d > 4000) {
							// We're far, go fast (boost the first turn, after that will default to 100)
							result[i] = Action.getMoveAction(target, maxThrust);
						} else {
							// Slow down
							result[i] = Action.getMoveAction(target, 75 + (int) Math.round(d / 4000 * 25));
						}

					}

				}

			}

			return result;
		}

	}

}
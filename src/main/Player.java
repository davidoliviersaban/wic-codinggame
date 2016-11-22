import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse the standard input
 * according to the problem statement. http://files.magusgeek.com/csb/csb.html
 **/
class Player {

	public static final boolean ACTIVATE_COLLISIONS = true;
	public static final int MAX_PODS = 2;
	public static int MAX_CHECKPOINTS = 10;
	
	/**
	 * Generic Point class
	 */
	public static class Point {
		double x, y;

		public Point() {
			this(0, 0);
		}

		public Point(double x, double y) {
			this.x = x;
			this.y = y;
		}

		double distance2(Point p) {
			return (this.x - p.x) * (this.x - p.x) + (this.y - p.y) * (this.y - p.y);
		}

		double distance(Point p) {
			return Math.sqrt(this.distance2(p));
		}

		/**
		 * This method returns the position of the impact
		 * 
		 * @param a
		 * @param b
		 * @return
		 */
		Point closest(Point a, Point b) {
			double da = b.y - a.y;
			double db = a.x - b.x;
			double c1 = da * a.x + db * a.y;
			double c2 = -db * this.x + da * this.y;
			double det = da * da + db * db;
			double cx = 0;
			double cy = 0;

			if (det != 0) {
				cx = (da * c1 - db * c2) / det;
				cy = (da * c2 + db * c1) / det;
			} else {
				// Le point est dÃ©jÃ  sur la droite
				cx = this.x;
				cy = this.y;
			}

			return new Point(cx, cy);
		}

		public String toString() {
			return String.format("{x:%.2f, y:%.2f}", x, y);
		}

	}

	/**
	 * Class responsible for handling collisions
	 */
	public static class Unit extends Point {
		int			id;
		double	vx, vy;
		double	radius;

		public Unit(Point p) {
			this(p.x, p.y);
		}

		public Unit(double x, double y) {
			super(x, y);
		}

		public Unit(int id, double x, double y) {
			this(x, y);
			this.id = id;
		}

		public Unit(int id, double x, double y, double vx, double vy, double r) {
			this(id, x, y);
			this.vy = vy;
			this.vx = vx;
			this.radius = r;
		}

		/**
		 * 
		 * @param pod1
		 * @param pod2
		 * @param noRadius
		 *          ne prend pas en compte le rayon
		 * @return if (alpha > 1 || alpha < 0) le choc n'a pas lieu entre les 2 pas
		 *         de temps
		 */
		public Collision collision2(Unit pod2) {
			double Vx = this.vx - pod2.vx;
			double Vy = this.vy - pod2.vy;
			double Px = this.x - pod2.x;
			double Py = this.y - pod2.y;
			double d2 = (radius + pod2.radius) * (radius + pod2.radius);
			double a = 2 * (Vx * Vx + Vy * Vy); // je calcule 2*a directement
			if (a == 0)
				return null;
			double b = 2 * (Px * Vx + Py * Vy);
			double c = Px * Px + Py * Py - d2;

			double delta = b * b - 2 * a * c;
			double t1 = (-b - Math.sqrt(delta)) / a;
			double t2 = (-b + Math.sqrt(delta)) / a;

			double alpha = (t1 < 0) ? t2 : Math.min(t1, t2);
			if (alpha < 0 || alpha > 1)
				return null; // no collision
			return new Collision(this, pod2, alpha);
		}

		public String toString() {
			return String.format("{id:%d, position:%s, vx:%.2f, vy:%.2f, radius:%d}", id, super.toString(), vx, vy, (int)radius);
		}

		public void bounce(Unit u) {

		}

	}

	public static class Checkpoint extends Unit {
		public static double CHECKPOINT_RADIUS = 600;

		public Checkpoint(int id, double x, double y) {
			super(id, x, y, 0, 0, CHECKPOINT_RADIUS);
		}
		
		public boolean equals(Checkpoint c) {
			return distance2(c) <= 0.000001;
		}
	}

	public static class Pod extends Unit {

		public static final double POD_RADIUS = 400;

		public static final double	MAX_ANGLE		= 18;
		public static final int			MAX_THRUST	= 100;
		public static final int			THRUST_IN_BOOST	= 650;
		public static final int			MAX_TIMEOUT	= 100;
		public static final int			SHIELD_UP		= 3;
		public static final int			MAX_BOOST_IN_RACE		= 1;
		public static final double 	FRICTION 		= 0.85;

		double	angle, thrust;
		int			timeout = MAX_TIMEOUT;
		int			shield;
		int			boost = MAX_BOOST_IN_RACE;

		public int nextCheckpointId;

		public Pod(int id, double x, double y, double vx, double vy) {
			super(id, x, y, vx, vy, POD_RADIUS);
		}

		public boolean equals(Pod pod2) {
			return distance2(pod2) <= 0.000001 && new Point(vx, vy).distance2(new Point(pod2.vx, pod2.vy)) <= 0.000001;
		}
		
		public String toString() {
			return String.format("{Unit: %s, angle: %d, thrust: %d, boost: %d, targetId: %d}",super.toString(),(int)angle,(int)thrust,boost,nextCheckpointId);
		}
		
		public boolean activateShield() {
			if (shield > 0) return false;
			shield = SHIELD_UP;
			return true;
		}
		public boolean activateBoost() {
			if (boost <= 0) {
				thrust = MAX_THRUST;
				return false;
			}
			boost--;
			thrust  = THRUST_IN_BOOST;
			return true;
		}

		public Pod clone() {
			Pod pod = new Pod(id, x, y, vx, vy);
			pod.shield = this.shield;
			pod.thrust = this.thrust;
			pod.angle = this.angle;
			pod.nextCheckpointId = this.nextCheckpointId;
			pod.timeout = this.timeout;
			return pod;
		}

		@SuppressWarnings("unused")
		public void bounce(Unit u) {
			if (ACTIVATE_COLLISIONS && (u instanceof Pod)) {
				Pod pod = (Pod) u;
				// Si un pod a son bouclier d'activé, sa masse est de 10, sinon
				// elle est de 1
				double m1 = (this.shield > 0) ? 10 : 1;
				double m2 = (pod.shield > 0) ? 10 : 1;

				// Si les masses sont égales, le coefficient sera de 2. Sinon il
				// sera de 11/10
				double mcoeff = (m1 + m2) / (m1 * m2);

				double nx = this.x - u.x;
				double ny = this.y - u.y;

				// Distance au carré entre les 2 pods. Cette valeur pourrait
				// être écrite en dure car ce sera toujours 800²
				double nxnysquare = nx * nx + ny * ny;

				double dvx = this.vx - u.vx;
				double dvy = this.vy - u.vy;

				// fx et fy sont les composantes du vecteur d'impact. product
				// est juste la pour optimiser
				double product = nx * dvx + ny * dvy;
				double fx = (nx * product) / (nxnysquare * mcoeff);
				double fy = (ny * product) / (nxnysquare * mcoeff);

				// On applique une fois le vecteur d'impact à chaque pod
				// proportionnellement à sa masse
				this.vx -= fx / m1;
				this.vy -= fy / m1;
				u.vx += fx / m2;
				u.vy += fy / m2;

				// Si la norme du vecteur d'impact est inférieur à 120, on
				// change sa norme pour le mettre à 120
				double impulse = Math.sqrt(fx * fx + fy * fy);
				if (impulse < 120.0) {
					fx = fx * 120.0 / impulse;
					fy = fy * 120.0 / impulse;
				}

				// On applique une deuxième fois le vecteur d'impact à chaque
				// pod proportionnellement à sa masse
				this.vx -= fx / m1;
				this.vy -= fy / m1;
				u.vx += fx / m2;
				u.vy += fy / m2;

				// C'est l'un des rares endroits où avoir une classe Vector
				// aurait rendu le code beaucoup plus lisible.
				// Mais cet endroit est appellé beaucoup trop souvent dans mon
				// code pour que je me permette de le rendre plus lisible au
				// prix de la performance
			} else if (u instanceof Checkpoint) {
				// On entre en collision avec un checkpoint
				this.bounceWithCheckpoint((Checkpoint) u);
			}
		}

		private void bounceWithCheckpoint(Checkpoint u) {
			if (++nextCheckpointId>=MAX_CHECKPOINTS) {
					nextCheckpointId = 0;
					timeout = MAX_TIMEOUT;
			}
		}

		/**
		 * Deplace le pod d'une grandeur
		 * 
		 * @param t
		 *          = 1.0 alors c'est un tour complet, sinon c'est un tour partiel
		 */
		void move(double t) {
			x += vx * t;
			y += vy * t;
		}

		void rotateDegree(double angle) {
			angle = (angle > MAX_ANGLE) ? MAX_ANGLE : angle;
			angle = (angle < -MAX_ANGLE) ? -MAX_ANGLE : angle;
			this.angle += angle;
			angle = (angle > 360) ? angle - 360 : angle;
			angle = (angle < 0) ? 360 - angle : angle;
		}

		void rotate(Point p) {
			rotateDegree(this.diffAngle(p));
		}

		double diffAngle(Point p) {
			double a = this.getAngle(p);

			// Pour connaitre le sens le plus proche, il suffit de regarder dans
			// les 2 sens et on garde le plus petit
			// Les opérateurs ternaires sont la uniquement pour éviter
			// l'utilisation d'un operateur % qui serait plus lent
			double right = this.angle <= a ? a - this.angle : 360.0 - this.angle + a;
			double left = this.angle >= a ? this.angle - a : this.angle + 360.0 - a;

			if (right < left) {
				return right;
			} else {
				// On donne un angle négatif s'il faut tourner à gauche
				return -left;
			}
		}

		double getAngle(Point p) {
			double d = this.distance(p);
			double dx = (p.x - this.x) / d;
			double dy = (p.y - this.y) / d;

			// Trigonométrie simple. On multiplie par 180.0 / PI pour convertir
			// en degré.
			double a = Math.toDegrees(Math.acos(dx));

			// Si le point qu'on veut est en dessus de nous, il faut décaler
			// l'angle pour qu'il soit correct.
			if (dy < 0) {
				a = 360.0 - a;
			}
			return a;
		}

		void boost(int thrust) {
			// N'oubliez pas qu'un pod qui a activé un shield ne peut pas
			// accélérer pendant 3 tours
			if (this.shield > 0) {
				return;
			}

			// Conversion de l'angle en radian
			double ra = Math.toRadians(this.angle);

			// Trigonométrie
			this.vx += Math.cos(ra) * thrust;
			this.vy += Math.sin(ra) * thrust;
		}

		void end() {
			this.x = Math.round(this.x);
			this.y = Math.round(this.y);
			this.vx = (int) (this.vx * FRICTION);
			this.vy = (int) (this.vy * FRICTION);

			// N'oubliez pas que le timeout descend de 1 chaque tour. Il revient
			// à 100 quand on passe par un checkpoint
			this.timeout--;
			if (this.shield > 0) shield--;
		}

		void play(Point p, int thrust) {
			this.rotate(p);
			this.boost(thrust);
			//this.move(1.0);
			//this.end();
		}

	}
	
	/**
	 * This object stores the timestamp of a collision between two units
	 */
	public static class Collision {
		Unit		a, b;
		double	t;

		public Collision(Unit a, Unit b, double time) {
			this.a = a;
			this.b = b;
			this.t = time;
		}

		public String toString() {
			return String.format("{unit-A:%s, unit-B:%s, t:%f}", a.toString(), b.toString(), t);
		}

	}

	public static class Move {
		int angle;
		int thrust;
		Point target;
		public Move(int angle, int thrust, Point target) {
			this.angle = angle;
			this.thrust = thrust;
			this.target = target;
		}
		public Move(double diffAngle, double thrust2, Point target2) {
			this((int)diffAngle,(int)thrust2,target2);
		}
		public String toStringOutput() {
			return String.format("%d %d %d", (int)Math.round(target.x),(int)Math.round(target.y),thrust); 
		}
	}
	
	
	public static class Simulator {

		@SuppressWarnings("unused")
		public static void play(Game game) {
			Pod[] pods = game.pods;
			Checkpoint[] checkpoints = game.checkpoints;
			// Il faut conserver le temps où on en est dans le tour. Le but est
			// d'arriver à 1.0
			double t = 0.0;

			while (t < 1.0) {
				Collision firstCollision = null;

				// On cherche toutes les collisions qui vont arriver pendant ce
				// tour
				for (int i = 0; i < pods.length; ++i) {
					// Collision avec un autre pod ?
					for (int j = i + 1; j < pods.length; ++j) {
						Collision col = pods[i].collision2(pods[j]);

						// Si la collision arrive plus tôt que celle qu'on,
						// alors on la garde
						if (ACTIVATE_COLLISIONS && col != null && col.t + t < 1.0 && (firstCollision == null || col.t < firstCollision.t)) {
							firstCollision = col;
						}
					}

					// Collision avec un checkpoint ?
					// Inutile de tester toutes les checkpoints ici. On test
					// juste le prochain checkpoint du pod.
					// On pourrait chercher les collisions du pod avec tous les
					// checkpoints, mais si une telle collision arrive elle
					// n'aura aucun impact sur le jeu de toutes façons
					Unit target = checkpoints[pods[i].nextCheckpointId];
					if (target == null) target = game.checkpoints[0];
					Collision col = pods[i].collision2(target);

					// Si la collision arrive plus tôt que celle qu'on, alors on
					// la garde
					if (col != null && col.t + t < 1.0 && (firstCollision == null || col.t < firstCollision.t)) {
						firstCollision = col;
					}
				}

				if (firstCollision == null) {
					// Aucune collision, on peut juste déplacer les pods jusqu'à
					// la fin du tour
					for (int i = 0; i < pods.length; ++i) {
						pods[i].move(1.0 - t);
					}

					// Fin du tour
					t = 1.0;
				} else {
					// On bouge les pods du temps qu'il faut pour arriver sur
					// l'instant `t` de la collision
					for (int i = 0; i < pods.length; ++i) {
						pods[i].move(firstCollision.t);
					}

					// On joue la collision
					firstCollision.a.bounce(firstCollision.b);

					t += firstCollision.t;
				}
			}

			// On arrondi les positions et on tronque les vitesses pour tout le
			// monde
			for (int i = 0; i < pods.length; ++i) {
				pods[i].end();
			}
		}
	}

	public static class IAWood {
		public static Move play(Game game, int podId) {
			Pod pod = game.pods[podId];
			Checkpoint target = game.checkpoints[pod.nextCheckpointId];
			if (target == null) target = game.checkpoints[0];
			double diffAngle = pod.diffAngle(target);
      pod.rotate(target);
			
			int thrust = 100;
      if (Math.abs(diffAngle) >= 90) {
          pod.thrust = 0;
      }
      else {
      	pod.thrust = 100;
      }
      pod.boost((int)pod.thrust);
      
      return new Move(diffAngle,thrust,target);
		}
	}
	public static class IABronze {
		public static Move play(Game game, int podId) {
			Pod pod = game.pods[podId];
			Checkpoint target = game.checkpoints[pod.nextCheckpointId];
			if (target == null) target = game.checkpoints[0];
			double diffAngle = pod.diffAngle(target);
      pod.rotate(target);
			
			int thrust = 100;
      if (Math.abs(diffAngle) >= 90) {
          pod.thrust = 0;
      }
      else {
      	pod.activateBoost();
      }
      pod.boost((int)pod.thrust);
      
      return new Move(diffAngle,thrust,target);
		}
	}
	
	public static class Game {
		public Pod[] pods = new Pod[MAX_PODS];
		public Checkpoint[] checkpoints = new Checkpoint[MAX_CHECKPOINTS];
		public double nextCheckpointDist, nextCheckpointAngle;
		
		public Game clone() {
			Game game = new Game();
			int i = 0;
			for (Pod pod : pods)
				game.pods[i++] = pod.clone();
			
			i = 0;
			for (Checkpoint cp : checkpoints)
				game.checkpoints[i++] = cp;
			game.nextCheckpointAngle = nextCheckpointAngle;
			game.nextCheckpointDist = nextCheckpointDist;
			
			return game;
		}
		
		public void initGame(Checkpoint cp, Pod[] pods) {
			
			double baryX = 0;
			double baryY = 0;
			for (int i  =0; i < pods.length; i++) {
				this.pods[i] = pods[i].clone();
				baryX += pods[i].x;
				baryY += pods[i].y;
			}
			
			checkpoints[0] = new Checkpoint(0,baryX/pods.length,baryY/pods.length);
			checkpoints[1] = cp;
			for (Pod pod : this.pods) {
				pod.nextCheckpointId = 1;
				pod.angle = pod.getAngle(checkpoints[pod.nextCheckpointId]);
			}
		}
		
		public void addCheckpoint(Checkpoint cp) {
			for (int i = 0; i < checkpoints.length; i++) {
				if (checkpoints[i] == null) checkpoints[i] = cp;
				else if (checkpoints[i].equals(cp)) break;
			}
		}
		
 		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("Pods: \n");
			for (Pod pod : pods) {
				sb.append(pod.toString()).append("\n");
			}
			sb.append("Checkpoints: \n");
			for (Checkpoint cp : checkpoints) {
				sb.append(cp.toString()).append("\n");
			}
			return sb.toString();
		}
		
	}

	private static Game initRound(Scanner in, Game g, boolean firstRound) {
		Game game = g.clone();
		int x = in.nextInt();
		int y = in.nextInt();
		int nextCheckpointX = in.nextInt(); // x position of the next checkpoint
		int nextCheckpointY = in.nextInt(); // y position of the next checkpoint
		game.nextCheckpointDist = in.nextInt(); // distance to the next checkpoint
		game.nextCheckpointAngle = in.nextInt(); // angle between your pod orientation and the direction of the next checkpoint
		int opponentX = in.nextInt();
		int opponentY = in.nextInt();

		Checkpoint nextCheckpoint = new Checkpoint(0, nextCheckpointX, nextCheckpointY);
		
		if (firstRound) {
			game.initGame(nextCheckpoint,new Pod[]{new Pod(0, x, y, 0, 0),new Pod(0, opponentX, opponentY, 0, 0)});
		} else {
			game.addCheckpoint(nextCheckpoint);
			game.pods[0].vx = x - game.pods[0].x;
			game.pods[0].vy = y - game.pods[0].y;
			game.pods[0].x = x;game.pods[0].y = y;
			
			game.pods[1].vx = opponentX - game.pods[1].x;
			game.pods[1].vy = opponentY - game.pods[1].y;
			game.pods[1].x = opponentX; game.pods[1].y = opponentY;
		}
		return game;
	}
	
	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		Game game = new Game();
		boolean firstRound = true;
		// game loop
		while (true) {
			initRound(in,game,firstRound);
			firstRound=false;
			System.out.println(IABronze.play(game, 0));
		}
	}
	
}

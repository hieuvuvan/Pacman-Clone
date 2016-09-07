package algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

import entity.Cell;
import entity.Entity;
import main.Map;

public class PathFinding {
	public static byte breadthFirstSearch(Cell start, Cell goals, byte dir, Map map) {
		if (goals.equals(start))
			return -1;
		Cell goal = new Cell();
		goal.set(goals);
		if (map.isWall(goal.row, goal.col)) {
			if (!map.isWall(goal.row - 1, goal.col - 1)) {
				goal.set(goal.row - 1, goal.col - 1);
			} else {
				if (!map.isWall(goal.row - 1, goal.col + 1)) {
					goal.set(goal.row - 1, goal.col + 1);
				} else {
					if (!map.isWall(goal.row + 1, goal.col + 1)) {
						goal.set(goal.row + 1, goal.col - 1);
					} else {
						if (!map.isWall(goal.row + 1, goal.col - 1)) {
							goal.set(goal.row + 1, goal.col - 1);
						} else {
							return -1;
						}
					}
				}
			}
		}
		int dx = 0, dy = 0;
		if (dir == Entity.UP)
			dy = -1;
		else if (dir == Entity.DOWN)
			dy = 1;
		else if (dir == Entity.LEFT)
			dx = -1;
		else
			dx = 1;

		LinkedList<Cell> queue = new LinkedList<>();
		HashMap<Integer, Integer> parent = new HashMap<>();
		Cell current;
		boolean existPath = false;

		queue.add(start);
		parent.put(start.getID(), -1);
		current = queue.removeFirst();
		if (current.equals(goal)) {
			existPath = true;
		} else {
			if (!map.isWall(start.row + dy, start.col + dx)) {
				Cell neighbor = new Cell(start.row + dy, start.col + dx);
				queue.add(neighbor);
				parent.put(neighbor.getID(), current.getID());
			}
			if (!map.isWall(start.row - dx, start.col + dy)) {
				Cell neighbor = new Cell(start.row - dx, start.col + dy);
				queue.add(neighbor);
				parent.put(neighbor.getID(), current.getID());
			}
			if (!map.isWall(start.row + dx, start.col - dy)) {
				Cell neighbor = new Cell(start.row + dx, start.col - dy);
				queue.add(neighbor);
				parent.put(neighbor.getID(), current.getID());
			}

			while (!queue.isEmpty()) {
				current = queue.removeFirst();
				if (current.equals(goal)) {
					existPath = true;
					break;
				}
				for (Cell neighbor : map.neighbors(current)) {
					if (!parent.containsKey(neighbor.getID())) {
						queue.add(neighbor);
						parent.put(neighbor.getID(), current.getID());
					}
				}
			}
		}
		if (existPath) {
			int currentID = goal.getID();
			while (parent.get(currentID) != start.getID()) {
				currentID = parent.get(currentID);
			}
			return getDirection(start, Cell.getCellFromID(currentID));
		} else
			return -1;
	}

	public static byte greedyBestFirstSearch(Cell start, Cell goals, byte dir, Map map) {
		if (goals.equals(start))
			return -1;
		Cell goal = new Cell();
		goal.set(goals);
		if (map.isWall(goal.row, goal.col)) {
			if (!map.isWall(goal.row - 1, goal.col - 1)) {
				goal.set(goal.row - 1, goal.col - 1);
			} else {
				if (!map.isWall(goal.row - 1, goal.col + 1)) {
					goal.set(goal.row - 1, goal.col + 1);
				} else {
					if (!map.isWall(goal.row + 1, goal.col + 1)) {
						goal.set(goal.row + 1, goal.col - 1);
					} else {
						if (!map.isWall(goal.row + 1, goal.col - 1)) {
							goal.set(goal.row + 1, goal.col - 1);
						} else {
							return -1;
						}
					}
				}
			}
		}
		int dx = 0, dy = 0;
		if (dir == Entity.UP)
			dy = -1;
		else if (dir == Entity.DOWN)
			dy = 1;
		else if (dir == Entity.LEFT)
			dx = -1;
		else
			dx = 1;

		PriorityQueue<Cell> queuePrior = new PriorityQueue<>((c1, c2) -> c1.prior - c2.prior);
		HashMap<Integer, Integer> parent = new HashMap<>();
		Cell current;
		boolean existPath = false;

		queuePrior.add(start);
		parent.put(start.getID(), -1);
		current = queuePrior.remove();
		if (current.equals(goal)) {
			existPath = true;
		} else {
			if (!map.isWall(start.row + dy, start.col + dx)) {
				Cell neighbor = new Cell(start.row + dy, start.col + dx);
				neighbor.prior = heuristic(neighbor, goal);
				queuePrior.add(neighbor);
				parent.put(neighbor.getID(), current.getID());
			}
			if (!map.isWall(start.row - dx, start.col + dy)) {
				Cell neighbor = new Cell(start.row - dx, start.col + dy);
				neighbor.prior = heuristic(neighbor, goal);
				queuePrior.add(neighbor);
				parent.put(neighbor.getID(), current.getID());
			}
			if (!map.isWall(start.row + dx, start.col - dy)) {
				Cell neighbor = new Cell(start.row + dx, start.col - dy);
				neighbor.prior = heuristic(neighbor, goal);
				queuePrior.add(neighbor);
				parent.put(neighbor.getID(), current.getID());
			}

			while (!queuePrior.isEmpty()) {
				current = queuePrior.remove();
				if (current.equals(goal)) {
					existPath = true;
					break;
				}
				for (Cell neighbor : map.neighbors(current)) {
					if (!parent.containsKey(neighbor.getID())) {
						neighbor.prior = heuristic(neighbor, goal);
						parent.put(neighbor.getID(), current.getID());
						queuePrior.add(neighbor);
					}
				}
			}
		}
		if (existPath) {
			int currentID = goal.getID();
			while (parent.get(currentID) != start.getID()) {
				currentID = parent.get(currentID);
			}
			return getDirection(start, Cell.getCellFromID(currentID));
		} else
			return -1;
	}

	public static byte aStar(Cell start, Cell goals, byte dir, Map map) {
		if (goals.equals(start))
			return -1;
		Cell goal = new Cell();
		goal.set(goals);
		if (map.isWall(goal.row, goal.col)) {
			if (!map.isWall(goal.row - 1, goal.col - 1)) {
				goal.set(goal.row - 1, goal.col - 1);
			} else {
				if (!map.isWall(goal.row - 1, goal.col + 1)) {
					goal.set(goal.row - 1, goal.col + 1);
				} else {
					if (!map.isWall(goal.row + 1, goal.col + 1)) {
						goal.set(goal.row + 1, goal.col - 1);
					} else {
						if (!map.isWall(goal.row + 1, goal.col - 1)) {
							goal.set(goal.row + 1, goal.col - 1);
						} else {
							return -1;
						}
					}
				}
			}
		}
		int dx = 0, dy = 0;
		if (dir == Entity.UP)
			dy = -1;
		else if (dir == Entity.DOWN)
			dy = 1;
		else if (dir == Entity.LEFT)
			dx = -1;
		else
			dx = 1;
		PriorityQueue<Cell> queuePrior = new PriorityQueue<>((c1, c2) -> c1.prior - c2.prior);
		HashMap<Integer, Integer> parent = new HashMap<>();
		HashMap<Integer, Integer> costFromStart = new HashMap<>();
		Cell current;
		boolean existPath = false;

		queuePrior.add(start);
		parent.put(start.getID(), -1);
		costFromStart.put(start.getID(), 0);
		current = queuePrior.remove();
		if (current.equals(goal)) {
			existPath = true;
		} else {
			if (!map.isWall(start.row + dy, start.col + dx)) {
				Cell neighbor = new Cell(start.row + dy, start.col + dx);
				costFromStart.put(neighbor.getID(), 1);
				neighbor.prior = 1 + heuristic(neighbor, goal);
				queuePrior.add(neighbor);
				parent.put(neighbor.getID(), current.getID());
			}
			if (!map.isWall(start.row - dx, start.col + dy)) {
				Cell neighbor = new Cell(start.row - dx, start.col + dy);
				costFromStart.put(neighbor.getID(), 1);
				neighbor.prior = 1 + heuristic(neighbor, goal);
				queuePrior.add(neighbor);
				parent.put(neighbor.getID(), current.getID());
			}
			if (!map.isWall(start.row + dx, start.col - dy)) {
				Cell neighbor = new Cell(start.row + dx, start.col - dy);
				costFromStart.put(neighbor.getID(), 1);
				neighbor.prior = 1 + heuristic(neighbor, goal);
				queuePrior.add(neighbor);
				parent.put(neighbor.getID(), current.getID());
			}
			while (!queuePrior.isEmpty()) {
				current = queuePrior.remove();
				if (current.equals(goal)) {
					existPath = true;
					break;
				}
				for (Cell neighbor : map.neighbors(current)) {
					int newCost = costFromStart.get(current.getID()) + 1;
					if (!costFromStart.containsKey(neighbor.getID()) || newCost < costFromStart.get(neighbor.getID())) {
						costFromStart.put(neighbor.getID(), newCost);
						neighbor.prior = newCost + heuristic(neighbor, goal);
						parent.put(neighbor.getID(), current.getID());
						queuePrior.add(neighbor);
					}
				}
			}
		}
		if (existPath) {
			int currentID = goal.getID();
			while (parent.get(currentID) != start.getID()) {
				currentID = parent.get(currentID);
			}
			return getDirection(start, Cell.getCellFromID(currentID));
		} else
			return -1;
	}

	public static Cell[] getPath(Cell start, Cell goal, Map map) {
		PriorityQueue<Cell> queuePrior = new PriorityQueue<>((c1, c2) -> c1.prior - c2.prior);
		HashMap<Integer, Integer> parent = new HashMap<>();
		HashMap<Integer, Integer> costFromStart = new HashMap<>();
		Cell current = start;

		queuePrior.add(start);
		parent.put(start.getID(), -1);
		costFromStart.put(start.getID(), 0);

		while (!queuePrior.isEmpty()) {
			current = queuePrior.remove();
			if (current.equals(goal)) {
				break;
			}
			for (Cell neighbor : map.neighbors(current)) {
				int newCost = costFromStart.get(current.getID()) + 1;
				if (!costFromStart.containsKey(neighbor.getID()) || newCost < costFromStart.get(neighbor.getID())) {
					costFromStart.put(neighbor.getID(), newCost);
					neighbor.prior = newCost + heuristic(neighbor, goal);
					parent.put(neighbor.getID(), current.getID());
					queuePrior.add(neighbor);
				}
			}
		}
		ArrayList<Cell> stack = new ArrayList<>();
		while (!current.equals(start)) {
			stack.add(current);
			current = Cell.getCellFromID(parent.get(current.getID()));
		}
		Cell[] path = new Cell[stack.size()];
		for (int i = 0; i < stack.size(); i++) {
			path[i] = stack.get(stack.size() - 1 - i);
		}
		return path;
	}

	public static int heuristic(Cell c1, Cell c2) {
		return Math.abs(c1.row - c2.row) + Math.abs(c1.col - c2.col);
	}

	public static byte getDirection(Cell start, Cell next) {
		if (next.row - start.row == 1)
			return Entity.DOWN;
		else if (next.row - start.row == -1)
			return Entity.UP;
		else if (next.col - start.col == 1)
			return Entity.RIGHT;
		else
			return Entity.LEFT;
	}
}

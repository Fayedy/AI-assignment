package controllers.Astar;

import java.util.*;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

/**
 *
 * @author fy
 */

class Node {
    ArrayList<Types.ACTIONS> actions;
    ArrayList<StateObservation> states;
    StateObservation self;
    double precost;
    double expcost;
    double value;

    Node() {
        actions = new ArrayList<>();
        states = new ArrayList<>();
        precost = 0;
        expcost = 0;
        value = 0;
    }

    Node(StateObservation s, double pre, double exp, ArrayList<Types.ACTIONS> act, ArrayList<StateObservation> st) {
        self = s;
        precost = pre;
        expcost = exp;
        value = precost + expcost;
        actions = new ArrayList<>();
        states = new ArrayList<>();
        actions.addAll(act);
        states.addAll(st);
    }
}

public class Agent extends AbstractPlayer {

    /**
     * Actions for the agent.
     */
    protected ArrayList<Types.ACTIONS> actions;

    /**
     * State Observations.
     */
    protected ArrayList<StateObservation> states;

    /**
     * If win the game.
     */
    protected boolean success;

    /**
     * The current step for agent.
     */
    protected int step;

    /**
     * The priority queue of current nodes.
     */
    protected PriorityQueue<Node> pqst;

    public static Comparator<Node> valueComparator = new Comparator<>() {
        @Override
        public int compare(Node o1, Node o2) {
            if(o1.value == o2.value) return 0;
            return o1.value > o2.value ? 1 : -1;
        }
    };

    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
    {
        actions = new ArrayList<> ();
        states = new ArrayList<> ();
        pqst = new PriorityQueue<>(valueComparator);
        success = false;
        step = 0;
    }



    /**
     * Count the steps should take to approach the target.
     * @param so state observation of the current game.
     * @return the number of steps.
     */

    private double heuristic(StateObservation so) {
        ArrayList<Observation>[] fixedPositions = so.getImmovablePositions();
        ArrayList<Observation>[] movingPositions = so.getMovablePositions();
        //System.out.println(fixedPositions.length);
        //System.out.println(fixedPositions[fixedPositions.length - 1].size());
        Vector2d goalpos = fixedPositions[fixedPositions.length - 1].get(0).position; //目标的坐标
        Vector2d avatarpos = so.getAvatarPosition(); //精灵的坐标


        //System.out.println(movingPositions.length);
        if(so.getAvatarType() != 4) { //没吃到钥匙
            Vector2d keypos = movingPositions[0].get(0).position; //钥匙的坐标
            if(fixedPositions.length > 2 && movingPositions.length > 1) {
                ArrayList<Observation> hole = fixedPositions[fixedPositions.length - 2];
                ArrayList<Observation> box = movingPositions[movingPositions.length - 1];
                if(hole != null && box != null && hole.size() > 0 && box.size() > 0) {
                    Vector2d holepos = hole.get(hole.size() - 1).position;
                    Vector2d boxpos = box.get(box.size() - 1).position;
                    return avatarpos.mdist(keypos) + 20 * avatarpos.mdist(boxpos) + boxpos.mdist(holepos);
                }
                //System.out.println("remove hole");
            }
            //System.out.println("no hole");
            return avatarpos.mdist(keypos) + avatarpos.mdist(goalpos); //曼哈顿距离和
        }
        else { //吃到要钥匙
            return avatarpos.mdist(goalpos);
        }
    }


    /**
     * Depth first search. This function is to find a way to win the game.
     * @param so state observation of the current game.
     * @return if the current state can approach the target.
     */

    private void ass(StateObservation so, ElapsedCpuTimer elapsedTimer) {
        long remaining = elapsedTimer.remainingTimeMillis();
        if(remaining < 5) return;

        ArrayList<Types.ACTIONS> subActions = so.getAvailableActions();
        Node cur = pqst.poll();
        //states.add(so);


        for (int i = 0; i < subActions.size(); i++) {
            StateObservation stCopy = so.copy();
            stCopy.advance(subActions.get(i));

            if (success) {
                //System.out.println("success");
                break;
            }

            if (stCopy.getGameWinner() == Types.WINNER.PLAYER_WINS) {
                success = true;
                System.out.println("success");

                actions.addAll(cur.actions);
                actions.add(subActions.get(i));
                states.addAll(cur.states);
                states.add(stCopy);
                break;
            }
            else if (stCopy.getGameWinner() == Types.WINNER.PLAYER_LOSES) {
                continue;
            }

            int j;
            for (j = 0; j < cur.states.size(); j++) {
                if (stCopy.equalPosition(cur.states.get(j)))
                    break;
            }
            if (j < cur.states.size()) continue;
            double he = heuristic(stCopy);
            j = 0;
            for(Node p : pqst) {
                if(stCopy.equalPosition(p.self) && cur.precost + 50 + he >= p.value) break;
                j++;
            }
            if(j < pqst.size()) continue;


            Node next = new Node(stCopy, cur.precost + 50, he, cur.actions, cur.states);
            next.actions.add(subActions.get(i));
            next.states.add(stCopy);

            pqst.add(next);
        }


        if(!success) {
            Node best = pqst.peek();
            if (best != null) {
                //System.out.println(best.self.getAvatarPosition());
                ass(best.self, elapsedTimer);
            }
            else
                System.out.println("error");
        }
    }


    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!success) {
            pqst.clear();
            Node start = new Node();
            start.precost = 0;
            start.expcost = heuristic(stateObs);
            start.value = start.precost + start.expcost;
            start.states.add(stateObs);
            pqst.add(start);
            ass(stateObs, elapsedTimer);
            if(!success) {
                Node best = pqst.poll();
                actions.add(best.actions.get(0));
            }
        }
        return actions.get(step++);
    }
}

package controllers.limitdepthfirst;

import java.util.ArrayList;

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
     * The limit of the depth.
     */
    protected int limit;

    /**
     * The good value of steps should take.
     */
    //protected double good;




    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
    {
        actions = new ArrayList<>();
        states = new ArrayList<>();
        success = false;
        step = 0;
        limit = 6;
        //good = 1000;
    }


    /**
     * Count the steps should take to approach the target.
     * @param so state observation of the current game.
     * @return the number of steps.
     */

    private double heuristic(StateObservation so) {
        ArrayList<Observation>[] fixedPositions = so.getImmovablePositions();
        ArrayList<Observation>[] movingPositions = so.getMovablePositions();
        Vector2d goalpos = fixedPositions[1].get(0).position; //目标的坐标
        Vector2d avatarpos = so.getAvatarPosition(); //精灵的坐标

        //System.out.println(movingPositions[0].size());
        if(so.getAvatarType() != 4) { //没吃到钥匙
            Vector2d keypos = movingPositions[0].get(0).position; //钥匙的坐标
            //System.out.println(keypos);
            return avatarpos.mdist(keypos) + avatarpos.mdist(goalpos); //曼哈顿距离和
            //return avatarpos.mdist(keypos);
        }
        else { //吃到钥匙
            //System.out.println(movingPositions[0].get(0).position);
            return avatarpos.mdist(goalpos);
        }
    }


    /**
     * Depth limited search. This function is to find a good way with limited depth.
     * @param so state observation of the current game.
     * @param depth the current depth of the search tree.
     * @return Steps the current state should take at least.
     */

    private double dls(StateObservation so, int depth) {
        ArrayList<Types.ACTIONS> subActions = so.getAvailableActions();
        states.add(so);
        double value = -1;
        double good = 1000;
        int goodact = 0;


        //System.out.println("depth:" + depth);

        if(depth == limit) {
            //System.out.println("limithe:" + heuristic(so));
            states.remove(states.size() - 1);
            return heuristic(so);
        }


        //System.out.println("act: " + subActions.size());

        for (int i = 0; i < subActions.size(); i++) {
            StateObservation stCopy = so.copy();
            stCopy.advance(subActions.get(i));
            if (success) {

                good = 0;
                break;
            }

            if (stCopy.getGameWinner() == Types.WINNER.PLAYER_WINS) {
                actions.add(subActions.get(i));
                success = true;
                //System.out.println("success");
                good = 0;
                break;
            }
            else if (stCopy.getGameWinner() == Types.WINNER.PLAYER_LOSES) {
                continue;
            }

            int j;
            for (j = 0; j < states.size(); j++) {
                if (stCopy.equalPosition(states.get(j)))
                    break;
            }
            if (j < states.size()) continue;

            //System.out.println("he: " + heuristic(stCopy));
            //if (heuristic(stCopy) == -1) continue;



            //System.out.println("act:" + i);
            actions.add(subActions.get(i));
            value = dls(stCopy, depth+1);
            if (!success) {
                actions.remove(actions.size() - 1);
            }
            if (value > 0 && value < good) {
                good = value;
                goodact = i;
            }
            /*else if (value > 0) {
                notgood = i;
                notgoodvalue = value;
            }*/
        }

        //if(depth == 0 && (value < 0 || value > good))
        //    actions.add(subActions.get(notgood));

        /*if(depth == 0) {
            System.out.println("goodvalue:" + good + " goodact:" + goodact);
        }
        System.out.println("goodact: " + goodact);*/
        if (!success && depth == 0)
            actions.add(subActions.get(goodact));

        if (!success && depth != 0)
            states.remove(states.size() - 1);

        //if (value < 0 || value > good)
        //    return notgoodvalue;
        return good;
    }





    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

        /*ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();
        while (elapsedTimerIteration.elapsedMillis() < 10000) {}*/

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!success) {

            dls(stateObs, 0);
        }
        //System.out.println("step: " + step);
        return actions.get(step++);
    }
}

package controllers.depthfirst;

import java.util.ArrayList;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

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
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
    {
        actions = new ArrayList<> ();
        states = new ArrayList<> ();
        success = false;
        step = 0;
    }


    /**
     * Depth first search. This function is to find a way to win the game.
     * @param so state observation of the current game.
     * @return if the current state can approach the target.
     */

     private boolean dfs(StateObservation so) {
        ArrayList<Types.ACTIONS> subActions = so.getAvailableActions();
        states.add(so);
        boolean way = false;

        for (int i = 0; i < subActions.size(); i++) {
            StateObservation stCopy = so.copy();
            stCopy.advance(subActions.get(i));
            if (success) {
                way = true;
                break;
            }

            if (stCopy.getGameWinner() == Types.WINNER.PLAYER_WINS) {
                actions.add(subActions.get(i));
                success = true;
                way = true;
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

            actions.add(subActions.get(i));
            way = dfs(stCopy);
            if(!way) {
                actions.remove(actions.size() - 1);
            }
            else break;
        }

        if(!way) {
            states.remove(states.size() - 1);
        }
        return way;
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
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (step == 0) {
            dfs(stateObs);
        }
        return actions.get(step++);
    }
}

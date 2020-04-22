package uk.ac.cam.ks920;

public class OutingOutOfMovestepBoundException extends Exception {

    OutingOutOfMovestepBoundException(int i1, int i2, int movestepsPerTimestep) {
        super(
                ((i1 < 0) ? " (Lower bound below zero) " : "") +
                        ((i2 > movestepsPerTimestep) ? " (Upper bound above number of movesteps per timestep) " : ""));
    }
}

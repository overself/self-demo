package com.hanwj.design.state.example;

public class LiftTest {


    public static void main(String[] args) {
        Lift lift = new Lift();
        lift.setState(new StoppingState(lift));
        lift.displayState();
        lift.open();
        lift.close();
        lift.run();
        //lift.open();
        lift.stop();
        lift.open();
    }


}

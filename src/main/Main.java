package main;

import org.openbw.bwapi4j.type.UnitType;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello my World!");

        for (UnitType unitType: UnitType.values()){
            System.out.println(unitType);
        }
    }

}

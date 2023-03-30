package com.ibrahimtahasanli.mlkit

import java.util.LinkedList

abstract class Training {
    public var poses: LinkedList<Angles> = LinkedList<Angles>();
    public var currentPose: Int=0;
    public var currentRepaet: Int = 0;

    public class Fitness{
        public var messages: LinkedList<String> = LinkedList<String>();
        public var value: Float = 0f;

        public fun Compare(other: Fitness): Boolean {
            var mes: MutableList<String> = other.messages.toMutableList()

            outer@for (i in this.messages){
                for ( j in 0 until mes.size){
                    if (i == mes[j]){
                        mes.removeAt(j)
                        continue@outer
                    }
                }

                return false
            }

            if (mes.size == 0)
                return true

            return false

        }
    }

    public abstract fun CheckFitness(curentPose: Angles): Fitness;
}
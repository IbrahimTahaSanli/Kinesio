package com.ibrahimtahasanli.mlkit

import com.google.mlkit.vision.pose.Pose

class SteppingUp : Training{
    constructor(){
        this.poses.add(
            Angles(
                180f,
                180f,
                0f,
                0f,
                0f,
                0f,
                180f,
                180f,
            )
        )

        this.poses.add(
            Angles(
                180f,
                130f,
                0f,
                0f,
                0f,
                0f,
                180f,
                130f,
            )
        )

    }

    private var DeadZone: Float = 25f;

    override fun CheckFitness(curent: Angles): Fitness {
        var retFit: Fitness = Fitness()

        when(this.currentPose){
            0->{
                if(this.poses[this.currentPose].leftLegDeg - curent.leftLegDeg > DeadZone){
                    if(!retFit.messages.contains("Your left leg must be straight"))
                        retFit.messages.add("Your left leg must be straight");
                }
                if(this.poses[this.currentPose].leftLegDeg - curent.leftLegDeg < -DeadZone){
                    if(!retFit.messages.contains("Your left leg must be straight"))
                        retFit.messages.add("Your left leg must be straight");
                }

                if(this.poses[this.currentPose].rightLegDeg - curent.rightLegDeg > DeadZone){
                    if(!retFit.messages.contains("Your right leg must be straight"))
                        retFit.messages.add("Your right leg must be straight");
                }
                if(this.poses[this.currentPose].rightLegDeg - curent.rightLegDeg < -DeadZone){
                    if(!retFit.messages.contains("Your right leg must be straight"))
                        retFit.messages.add("Your right leg must be straight");
                }

                if(this.poses[this.currentPose].leftHipDeg - curent.leftHipDeg > DeadZone){
                    if(!retFit.messages.contains("Your right leg must be straight"))
                        retFit.messages.add("Your right leg must be straight");
                }
                if(this.poses[this.currentPose].leftHipDeg - curent.leftHipDeg < -DeadZone){
                    if(!retFit.messages.contains("Your right leg must be straight"))
                        retFit.messages.add("Your right leg must be straight");
                }

                if(this.poses[this.currentPose].rightHipDeg - curent.rightHipDeg > DeadZone){
                    if(!retFit.messages.contains("Your right leg must be straight"))
                        retFit.messages.add("Your right leg must be straight");
                }
                if(this.poses[this.currentPose].rightHipDeg - curent.rightHipDeg < -DeadZone){
                    if(!retFit.messages.contains("Your right leg must be straight"))
                        retFit.messages.add("Your right leg must be straight");
                }

                if(retFit.messages.isEmpty()){
                    this.currentPose++
                }
            }
            1->{
                if(this.poses[this.currentPose].leftLegDeg - curent.leftLegDeg > DeadZone){
                    if(!retFit.messages.contains("Your left leg must be straight"))
                        retFit.messages.add("Your left leg must be straight");
                }
                if(this.poses[this.currentPose].leftLegDeg - curent.leftLegDeg < -DeadZone){
                    if(!retFit.messages.contains("Your left leg must be straight"))
                        retFit.messages.add("Your left leg must be straight");
                }

                if(this.poses[this.currentPose].rightHipDeg - curent.rightHipDeg > DeadZone){
                    if(!retFit.messages.contains("Your right shouldn't be this above"))
                        retFit.messages.add("Your right shouldn't be this above");
                }
                if(this.poses[this.currentPose].rightHipDeg - curent.rightHipDeg < -DeadZone){
                    if(!retFit.messages.contains("You have to raise your right leg"))
                        retFit.messages.add("You have to raise your right leg");
                }

                if(this.poses[this.currentPose].leftHipDeg - curent.leftHipDeg > DeadZone){
                    if(!retFit.messages.contains("Your left leg must be straight"))
                        retFit.messages.add("Your left leg must be straight");
                }
                if(this.poses[this.currentPose].leftHipDeg - curent.leftHipDeg < -DeadZone){
                    if(!retFit.messages.contains("Your left leg must be straight"))
                        retFit.messages.add("Your left leg must be straight");
                }

                if(this.poses[this.currentPose].rightLegDeg - curent.rightLegDeg > DeadZone){
                    if(!retFit.messages.contains("You have to pull your ankle back"))
                        retFit.messages.add("You have to pull your ankle back");
                }
                if(this.poses[this.currentPose].rightLegDeg - curent.rightLegDeg < -DeadZone){
                    if(!retFit.messages.contains("You have to push your ankle forward"))
                        retFit.messages.add("You have to push your ankle forward");
                }

                if(retFit.messages.isEmpty()){
                    this.currentPose == 0
                    this.currentRepaet++
                }
            }
        }

        return retFit
    }

}
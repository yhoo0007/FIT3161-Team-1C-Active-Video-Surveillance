# Active Video Surveillance System

## Introduction
This repository contains the code base for the active video surveillance system developed for FIT3161 and FIT3162 computer science final year project. The aim of this project was to develop a scalable active video surveillance system.

IP cameras can be connected to the system which will read and process the video frames in real time. Ideally, the system would be able to ingest video frames with sub-second latency and at a respectable frame rate.

## System Architecture

![System architecture](/imgs/low_level_architecture.png).

IP cameras act as the source of video frames into the system. They are connected to Kafka producers which packages and publishes them to the Kafka cluster.

The Kafka cluster can then serve the frames to consumer group(s) which can perform arbitrary processing on the frames. The processed frames are then published back into the Kafka cluster.

These frames can then be ingested by one or more consumer groups to be archived, displayed, etc.

In the latest implementation of the system, the processing group performs face detection and counting. The processed frames are consumed by two consumer groups: one to display the information on a dashboard, and another to archive the video and extracted information into a HDFS cluster and MongoDB database respectively.

## Outcomes
In summary, the system is able to satisfactorily meet the requirements initially set out for the project provided that a certain conditions are met, e.g. number of video sources, system specifications, etc. 

A comprehensive report on the outcomes of this project can be found in the reports folder ( /reports/ ):


* [Final Report](https://github.com/yhoo0007/FIT3161-Team-1C-Active-Video-Surveillance/blob/master/reports/Team1C_FinalReport.pdf)
* [Code Report](https://github.com/yhoo0007/FIT3161-Team-1C-Active-Video-Surveillance/blob/master/reports/Team1C_CodeReport.pdf)
* [Test Report](https://github.com/yhoo0007/FIT3161-Team-1C-Active-Video-Surveillance/blob/master/reports/Team1C_TestReport.pdf)
* [Team Management Report](https://github.com/yhoo0007/FIT3161-Team-1C-Active-Video-Surveillance/blob/master/reports/Team1C_TeamManagementReport.pdf)

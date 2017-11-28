# Myo-HMI-Android

A mobile Android application used to implement SFSU ICE lab Myo Human Machine Interface Gesture Recognition Algorithms

This application is to be paired with Thalmic Labs Myo Armband, a low cost emg/imu wearable sensor. The app connects to the armband via bluetooth and reads EMG data in real time. EMG signals are subjected to time domain feature extraction and passed to Machine Learning algorithms with the help of the SMILE Java Machine Learning libraries. After proper training, the app can predict most hand gestures.

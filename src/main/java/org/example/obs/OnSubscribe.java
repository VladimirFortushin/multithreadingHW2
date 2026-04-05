package org.example.obs;

public interface OnSubscribe<T> {
    void call(Observer<T> observer);
}

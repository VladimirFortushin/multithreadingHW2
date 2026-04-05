package org.example;

import org.example.obs.Observable;
import org.example.obs.Observer;

public class Main {
    static void main(String[] args) {

        Observable<Integer> observable = Observable.create(o -> {
            o.onNext(1);
            o.onNext(2);
            o.onNext(3);
            o.onComplete();
        });

        observable
                .map(x -> x * 2)
                .filter(x -> x > 2)
                .subscribe(new Observer<Integer>() {
                    public void onNext(Integer item) {
                        System.out.println(item);
                    }

                    public void onError(Throwable e) {
                        System.out.println("Ошибка");
                    }

                    public void onComplete() {
                        System.out.println("Готово");
                    }
                });
    }
}

import org.example.obs.Disposable;
import org.example.obs.Observable;
import org.example.obs.Observer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PatternTest {

    @Test
    public void testDisposable(){
        List<Integer> result = new ArrayList<>();
        Disposable d = Observable.create((Observer<Integer> o) -> {
            o.onNext(1);
            o.onNext(2);
            o.onNext(3);
        }).subscribe(new Observer<Integer>() {
            public void onNext(Integer item) {
                result.add(item);
            }
            public void onError(Throwable e) {}

            public void onComplete() {}
        });
        d.dispose();
        assertTrue(d.isDisposed());
    }

    @Test
    public void testMap() {
        List<Integer> result = new ArrayList<>();
        Observable.create((Observer<Integer> o) -> {
                    o.onNext(1);
                    o.onNext(2);
                    o.onComplete();
                })
                .map(x -> x * 2)
                .subscribe(new Observer<Integer>() {
                    public void onNext(Integer item) {
                        result.add(item);
                    }
                    public void onError(Throwable e) {}
                    public void onComplete() {}
                });
        assertEquals(List.of(2, 4), result);
    }

    @Test
    public void testFilter() {
        List<Integer> result = new ArrayList<>();
        Observable.create((Observer<Integer> o) -> {
                    o.onNext(1);
                    o.onNext(2);
                    o.onNext(3);
                    o.onComplete();
                })
                .filter(x -> x > 1)
                .subscribe(new Observer<Integer>() {
                    public void onNext(Integer item) {
                        result.add(item);
                    }
                    public void onError(Throwable e) {}
                    public void onComplete() {}
                });
        assertEquals(List.of(2, 3), result);
    }

    @Test
    public void testError(){
        AtomicBoolean errorCalled = new AtomicBoolean(false);
        Observable.create((Observer<Integer> o) -> {
                    throw new RuntimeException("fail");
                })
                .subscribe(new Observer<Integer>() {
                    public void onNext(Integer item) {}

                    public void onError(Throwable e) {
                        errorCalled.set(true);
                    }
                    public void onComplete() {}
                });
        assertTrue(errorCalled.get());
    }
}

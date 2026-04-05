package org.example.obs;

import org.example.schedule.Scheduler;

import java.util.function.Function;
import java.util.function.Predicate;

public class Observable<T> {

    private OnSubscribe<T> onSubscribe;

    private Observable(OnSubscribe<T> onSubscribe) {
        this.onSubscribe = onSubscribe;
    }

    public static <T> Observable<T> create(OnSubscribe<T> onSubscribe) {
        return new Observable<>(onSubscribe);
    }

    public Disposable subscribe(Observer<T> observer) {
        final boolean[] disposed = {false};

        try {
            onSubscribe.call(new Observer<T>() {
                @Override
                public void onNext(T item) {
                    if (!disposed[0]) observer.onNext(item);
                }

                @Override
                public void onError(Throwable e) {
                    if (!disposed[0]) observer.onError(e);
                }

                @Override
                public void onComplete() {
                    if (!disposed[0]) observer.onComplete();
                }
            });
        } catch (Throwable e) {
            observer.onError(e);
        }

        return new Disposable() {
            public void dispose() {
                disposed[0] = true;
            }

            public boolean isDisposed() {
                return disposed[0];
            }
        };
    }

    public <R> Observable<R> map(Function<T, R> mapper) {
        return Observable.create(observer ->
                this.subscribe(new Observer<T>() {
                    public void onNext(T item) {
                        observer.onNext(mapper.apply(item));
                    }

                    public void onError(Throwable e) {
                        observer.onError(e);
                    }

                    public void onComplete() {
                        observer.onComplete();
                    }
                })
        );
    }

    public Observable<T> filter(Predicate<T> predicate) {
        return Observable.create(observer ->
                this.subscribe(new Observer<T>() {
                    public void onNext(T item) {
                        if (predicate.test(item)) {
                            observer.onNext(item);
                        }
                    }

                    public void onError(Throwable e) {
                        observer.onError(e);
                    }

                    public void onComplete() {
                        observer.onComplete();
                    }
                })
        );
    }

    public Observable<T> subscribeOn(Scheduler scheduler) {
        return create(observer -> {
            scheduler.execute(() -> Observable.this.subscribe(observer));
        });
    }

    public Observable<T> observeOn(Scheduler scheduler) {
        return create(observer -> {
            subscribe(new Observer<T>() {
                @Override
                public void onNext(T item) {
                    scheduler.execute(() -> observer.onNext(item));
                }
                @Override
                public void onError(Throwable t) {
                    scheduler.execute(() -> observer.onError(t));
                }
                @Override
                public void onComplete() {
                    scheduler.execute(() -> observer.onComplete());
                }
            });
        });
    }

    public <R> Observable<R> flatMap(Function<T, Observable<R>> mapper) {
        return create(observer -> {
            subscribe(new Observer<T>() {
                @Override
                public void onNext(T item) {
                    try {
                        mapper.apply(item).subscribe(new Observer<R>() {
                            @Override
                            public void onNext(R r) {
                                observer.onNext(r);
                            }
                            @Override
                            public void onError(Throwable t) {
                                observer.onError(t);
                            }
                            @Override
                            public void onComplete() {
                                // не завершаем внешний поток
                            }
                        });
                    } catch (Throwable t) {
                        observer.onError(t);
                    }
                }
                @Override
                public void onError(Throwable t) {
                    observer.onError(t);
                }
                @Override
                public void onComplete() {
                    observer.onComplete();
                }
            });
        });
    }
}
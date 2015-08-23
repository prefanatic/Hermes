package edu.uri.egr.hermes.wrappers;

import android.support.v4.util.SimpleArrayMap;

import edu.uri.egr.hermes.Hermes;
import edu.uri.egr.hermes.exceptions.HermesException;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by cody on 8/20/15.
 */
public class RxDispatchWrapper {
    private final Hermes hermes = Hermes.get();
    private final SimpleArrayMap<Integer, PublishSubject<?>> subjectMap = new SimpleArrayMap();
    private final SimpleArrayMap<Integer, Class> classMap = new SimpleArrayMap<>();

    private static RxDispatchWrapper instance;

    private RxDispatchWrapper() {
    }

    public static RxDispatchWrapper get() {
        if (instance != null)
            return instance;
        return new RxDispatchWrapper();
    }

    public <T> PublishSubject<T> createSubject(int key) {
        return (PublishSubject<T>) subjectMap.put(key, PublishSubject.create());
    }

    public <T> PublishSubject<T> getSubject(int key) {
        if (!subjectMap.containsKey(key))
            return createSubject(key);

        return (PublishSubject<T>) subjectMap.get(key);
    }

    public <T> Observable<T> getObserver(int key) {
        if (!subjectMap.containsKey(key))
            throw new HermesException("No observable found under key " + key);

        return (Observable<T>) subjectMap.get(key).asObservable();
    }
}

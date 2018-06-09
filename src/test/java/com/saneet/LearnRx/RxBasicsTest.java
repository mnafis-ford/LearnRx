package com.saneet.LearnRx;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class RxBasicsTest {

    private ObservableFactory observableFactory;

    @Mock
    Random random;

    private RxBasics subject;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        observableFactory = spy(new ObservableFactory(random));
        subject = new RxBasics(observableFactory);
    }

    @Test
    public void listenToObservable() {
        when(random.nextInt()).thenReturn(6);

        subject.listenToObservable();

        verify(observableFactory).singleItemObservable();
        verifyNoMoreInteractions(observableFactory);
        assertThat(subject.getOutputs()).isNotEmpty();
        assertThat(subject.getOutputs().get(0)).isEqualTo(6);
    }

    @Test
    public void chainObservables() {
        when(random.nextInt()).thenReturn(6, 7, 8);

        subject.chainObservables();

        verify(observableFactory).singleItemObservable();
        verify(observableFactory).additionObservable(6);
        verify(observableFactory).subtractionObservable(13);
        verify(observableFactory).convertToString(5);
        verifyNoMoreInteractions(observableFactory);
        assertThat(subject.getOutputs()).isNotEmpty();
        assertThat(subject.getOutputs().get(0)).isEqualTo("5");
    }

    @Test
    public void modifyEmissionsReturnList() {
        when(random.nextInt()).thenReturn(6, 7, 8, 9, 0);

        List<String> result = subject.modifyEmissionsReturnList();

        verify(observableFactory).seriesObservable();
        verifyNoMoreInteractions(observableFactory);
        assertThat(result).isEqualTo(Arrays.asList("num6", "num7", "num8", "num9", "num0"));
    }

    @Test
    public void chainWithCarryOverValue_useSameOperatorFromChainObservablesTest() {
        when(random.nextInt()).thenReturn(5, 6);

        subject.chainWithCarryOverValue();
        verify(observableFactory).delayObservable();
        reset(observableFactory);

        giveItASecond();
        giveItASecond();

        verify(observableFactory).delayObservable();
        verifyNoMoreInteractions(observableFactory);
        assertThat(subject.getOutputs()).isNotEmpty();
        assertThat(subject.getOutputs().get(0)).isEqualTo(5 + 6);
    }

    @Test
    public void parallelObservables() {
        when(random.nextInt()).thenReturn(5, 6);

        subject.parallelObservables();
        giveItASecond();

        verify(observableFactory, times(2)).delayObservable();
        verifyNoMoreInteractions(observableFactory);
        assertThat(subject.getOutputs()).isNotEmpty();
        assertThat(subject.getOutputs().get(0)).isEqualTo(5 + 6);
    }

    @Test
    public void multipleSubscribersScenario1_newItemEachTime() {
        when(random.nextInt()).thenReturn(5, 6, 7);

        Observable<Integer> observable = subject.multipleSubscribersScenario1();
        assertThat(observable).isNotNull();

        List<String> outputs = new ArrayList<>();
        observable.subscribe(i -> outputs.add("First -> " + i)); giveItAMoment();//Ignore. This is used to make sure thread outputs don't overlap.
        observable.subscribe(i -> outputs.add("Second -> " + i)); giveItAMoment();//Ignore. This is used to make sure thread outputs don't overlap.
        observable.subscribe(i -> outputs.add("Third -> " + i)); giveItAMoment();//Ignore. This is used to make sure thread outputs don't overlap.
        observable.subscribe(i -> outputs.add("Fourth -> " + i)); giveItAMoment();//Ignore. This is used to make sure thread outputs don't overlap.
        observable.subscribe(i -> outputs.add("Fifth -> " + i)); giveItAMoment();//Ignore. This is used to make sure thread outputs don't overlap.
        giveItASecond();

        verify(observableFactory).delayObservable();
        verifyNoMoreInteractions(observableFactory);
        assertThat(outputs).containsAll(Arrays.asList("First -> 5",
                "Second -> 6",
                "Third -> 7",
                "Fourth -> 7",
                "Fifth -> 7"));
    }

    @Test
    public void multipleSubscribersScenario2_sameItemEachTime() {
        when(random.nextInt()).thenReturn(5, 6, 7);

        Observable<Integer> observable = subject.multipleSubscribersScenario2();
        assertThat(observable).isNotNull();

        List<String> outputs = new ArrayList<>();
        observable.subscribe(i -> outputs.add("First -> " + i));
        observable.subscribe(i -> outputs.add("Second -> " + i));
        observable.subscribe(i -> outputs.add("Third -> " + i));
        observable.subscribe(i -> outputs.add("Fourth -> " + i));
        observable.subscribe(i -> outputs.add("Fifth -> " + i));
        giveItASecond();

        verify(observableFactory).delayObservable();
        verifyNoMoreInteractions(observableFactory);
        assertThat(outputs).containsAll(Arrays.asList("First -> 5",
                "Second -> 5",
                "Third -> 5",
                "Fourth -> 5",
                "Fifth -> 5"));
    }

    @Test
    public void multipleSubscribersScenario3_sameItemWhileSubscribed_newItemOnDisconnect() {
        when(random.nextInt()).thenReturn(5, 6, 7);

        Observable<Integer> observable = subject.multipleSubscribersScenario3();
        assertThat(observable).isNotNull();

        List<String> outputs = new ArrayList<>();
        observable.subscribe(i -> outputs.add("First -> " + i));
        observable.subscribe(i -> outputs.add("Second -> " + i));
        observable.subscribe(i -> outputs.add("Third -> " + i));

        giveItASecond();

        observable.subscribe(i -> outputs.add("Fourth -> " + i));
        observable.subscribe(i -> outputs.add("Fifth -> " + i));

        giveItASecond();

        observable.subscribe(i -> outputs.add("Sixth -> " + i));

        giveItASecond();

        verify(observableFactory).delayObservable();
        verifyNoMoreInteractions(observableFactory);
        assertThat(outputs).containsAll(Arrays.asList("First -> 5",
                "Second -> 5",
                "Third -> 5",
                "Fourth -> 6",
                "Fifth -> 6",
                "Sixth -> 7"));
    }

    private void giveItASecond() {
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void giveItAMoment() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
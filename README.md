Реализованная библиотека представляет собой упрощённую версию реактивной библиотеки RxJava, построенную на основе паттерна «Наблюдатель» (Observer) с поддержкой асинхронных операций и управления подписками.

Observable<T> - Источник данных, который может излучать последовательность событий
Observer<T> - потребитель данных, реагирующий на события onNext, onError, onComplete
OnSubscribe<T> - функциональный интерфейс, описывающий логику эмиссии данных
Disposable - управление подпиской (возможность отписаться от потока)
Scheduler - абстракция для выполнения задач в различных потоках
Function<T,R> - функция преобразования одного типа в другой
Predicate<T> - функция-предикат для фильтрации значений

Observable реализует вычисления: цепочка операторов (map, filter, flatMap и др.) создаёт новый Observable, который оборачивает предыдущий. 
Эмиссия данных начинается при вызове метода subscribe().
Каждый оператор создаёт новый Observable с помощью Observable.create(), внутри которого подписывается на исходный Observable и трансформирует события перед передачей их дальше.
Observable.create() -> 
.map(Function<T,R>) - преобразование - <R> - применяет функцию к каждому onNext и передаёт результат дальше
.filter(Predicate<T>) - фильтрация - передаёт элемент дальше только если predicate.test(item) == true
.flatMap(Function<T, Observable<R>>) - "разворачивание" потоков -  <R> - каждый элемент превращается в отдельный Observable, чьи события передаются в результирующий поток
.subscribeOn(Scheduler) - определяет поток для кода внутри create() (для выполнения всего блока OnSubscribe.call())
.observeOn(Scheduler) - определяет поток для кода в Observer - методы onNext/onError/onComplete вызываются на указанном Scheduler (определяет поток, в котором будут вызываться методы onNext, onError, onComplete у подписчика (Observer))

Механизм Disposable (управление подпиской)
При вызове dispose() флаг меняется на true, и все последующие события (onNext, onError, onComplete) игнорируются.
subscribe(Observer) ->
Disposable
.dispose()
.isDisposed() 

Scheduler  - позволяет управлять тем, в каком потоке выполняется код эмиссии данных (subscribeOn) и в каком потоке обрабатываются результаты (observeOn).
Scheduler ->
ComputationScheduler - CPU cores + Multithreading потоков - для задач, которые нагружают процессор (математические расчёты, обработка изображений, шифрование).
IOScheduler - кэшируемый пул - для задач, которые ждут внешних операций (диск, сеть, БД), здесь потоки большую часть времени находятся в ожидании.
SingleThreadScheduler - один поток - когда важна строгая последовательность обработки событий или требуется потокобезопасность без лишних синхронизаций.

Тесты:
Класс PatternTest, использован JUnit5

.testDisposable() - проверка Disposable (отписка) - после вызова dispose() подписка помечается как разорванная.
.testMap() - проверка оператора map - каждый элемент правильно преобразуется функцией.
.testFilter() - проверка оператора filter - элементы, не удовлетворяющие предикату, исключаются из потока.
.testError() - проверка обработки ошибок - исключения в OnSubscribe.call() корректно перехватываются и передаются в observer.onError().
.testFlatMap() - Базовое разворачивание потоков
.testFlatMap_WithError() - Ошибки внутри flatMap
.testSubscribeOn() - тест проверяет, что subscribeOn() переключает ВСЮ цепочку операторов (включая map) в указанный поток.
.testComputationScheduler() - проверяет, что ComputationScheduler создаёт ограниченное количество потоков (≈ числу ядер процессора), а не бесконечное.
.testIOScheduler() - проверяет, что IOScheduler может создавать несколько потоков (в отличие от SingleThreadScheduler), но в разумных пределах.
.testSingleThreadScheduler() - проверяет, что SingleThreadScheduler использует ровно один поток для всех задач, даже если их много.

Практический пример:
Вычисления:
Observable.from(listOfNumbers)
    .subscribeOn(new ComputationScheduler()) - создаем потоки в количестве CPU+Hyperthreading
    .map(n -> heavyComputation(n)) - тяжёлая математика
    .observeOn(new IOScheduler()) - переключаемся на I/O
    .subscribe(result -> {
        saveToDatabase(result);  -  запись в БД
    });

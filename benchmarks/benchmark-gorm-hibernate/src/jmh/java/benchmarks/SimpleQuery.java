/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package benchmarks;

import example.Book;
import example.BookRepository;
import groovy.lang.Closure;
import org.grails.orm.hibernate.HibernateDatastore;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Arrays;

@State(Scope.Benchmark)
public class SimpleQuery {

    HibernateDatastore datastore;
    BookRepository bookRepository;

    @Setup
    public void prepare() {
        this.datastore = new HibernateDatastore(Book.class);
        this.bookRepository = datastore.getService(BookRepository.class);

        Book.withTransaction(new Closure<Object>(this) {
            public Object doCall(Object o) {
                Book.saveAll(Arrays.asList(
                        new Book("The Stand", 1000),
                        new Book("The Shining", 600),
                        new Book("The Power of the Dog", 500),
                        new Book("The Border", 700),
                        new Book("Along Came a Spider", 300),
                        new Book("Pet Cemetery", 400),
                        new Book("A Game of Thrones", 900),
                        new Book("A Clash of Kings", 1100)
                ));
                return null;
            }
        });

    }

    @TearDown
    public void cleanup() {
        datastore.close();
    }

    @Benchmark
    public void measureFinder() {
        bookRepository.findByTitle("The Border");
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*" + SimpleQuery.class.getSimpleName() + ".*")
                .warmupIterations(3)
                .measurementIterations(4)
                .forks(1)
//                .jvmArgs("-agentpath:/Applications/YourKit-Java-Profiler-2018.04.app/Contents/Resources/bin/mac/libyjpagent.jnilib")
                .build();

        new Runner(opt).run();
    }

}

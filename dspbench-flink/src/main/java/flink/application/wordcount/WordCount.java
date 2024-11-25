package flink.application.wordcount;

import flink.application.AbstractApplication;
import flink.constants.WordCountConstants;
import flink.parsers.StringParser;
import flink.source.InfSourceFunction;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class WordCount extends AbstractApplication {

    private static final Logger LOG = LoggerFactory.getLogger(WordCount.class);

    private int parserThreads;
    private int splitSentenceThreads;
    private int wordCountThreads;

    public WordCount(String appName, Configuration config) {
        super(appName, config);
    }

    @Override
    public void initialize() {
        parserThreads = config.getInteger(WordCountConstants.Conf.PARSER_THREADS, 1);
        splitSentenceThreads = config.getInteger(WordCountConstants.Conf.SPLITTER_THREADS, 1);
        wordCountThreads = config.getInteger(WordCountConstants.Conf.COUNTER_THREADS, 1);
    }

    @Override
    public StreamExecutionEnvironment buildApplication() {

        env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<Tuple1<String>> dataParse = env.addSource(new WCInfSource(config, getConfigPrefix())).setParallelism(parserThreads);
//        // Spout
//        DataStream<String> data = createSource();
//
//        // Parser
//        DataStream<Tuple1<String>> dataParse = data.flatMap(new StringParser(config)).setParallelism(parserThreads);

        // Process
        DataStream<Tuple2<String, Integer>> splitter = dataParse.filter(value -> (value.f0 != null))
                .flatMap(new Splitter(config)).setParallelism(splitSentenceThreads);

        DataStream<Tuple2<String, Integer>> count = splitter.keyBy(value -> value.f0).flatMap(new Counter(config))
                .setParallelism(wordCountThreads);

        // Sink
        createSinkWC(count);

        return env;
    }

    @Override
    public String getConfigPrefix() {
        return WordCountConstants.PREFIX;
    }

    @Override
    public Logger getLogger() {
        return LOG;
    }
}
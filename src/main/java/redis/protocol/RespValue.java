package redis.protocol;

import java.util.List;

public sealed interface RespValue
        permits RespValue.SimpleString,
        RespValue.Error,
        RespValue.IntegerValue,
        RespValue.BulkString,
        RespValue.Array,
        RespValue.NullValue {

    record SimpleString(String value) implements RespValue {}

    record Error(String value) implements RespValue {}

    record IntegerValue(long value) implements RespValue {}

    record BulkString(String value) implements RespValue {}

    record Array(List<RespValue> values) implements RespValue {}

    record NullValue() implements RespValue {}
}
CREATE TABLE `{ydb_locker_table}` (
    object_id Utf8 NOT NULL, -- идентификатор заблокированной записи
    h_type Utf8, -- код вида обработчика
	h_instance Utf8, -- экземпляр одного из N обработчиков данного вида
	PRIMARY KEY(object_id),
	INDEX ix_owner GLOBAL ON (h_type, h_instance)
) WITH (
	AUTO_PARTITIONING_BY_LOAD = ENABLED,
        AUTO_PARTITIONING_MIN_PARTITIONS_COUNT = 100,
        AUTO_PARTITIONING_MAX_PARTITIONS_COUNT = 150
);

DECLARE $h_type AS Utf8;
DECLARE $h_instance AS Utf8;
DECLARE $object_id AS List<Utf8>;

SELECT ENSURE(1, h_type=$h_type AND h_instance=$h_instance,
              UNWRAP('LOCK_CONFLICT on '||h_type||':'||h_instance)) AS qq
FROM `{ydb_locker_table}`
WHERE object_id IN $object_ids;

UPSERT INTO `{ydb_locker_table}`
SELECT object_id, $h_type AS h_type, $h_instance AS h_instance
FROM AS_TABLE(ListMap($object_ids, ($x) -> { RETURN AsStruct($x AS object_id); }));

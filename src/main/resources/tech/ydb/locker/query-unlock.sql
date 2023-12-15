DECLARE $h_type AS Utf8;
DECLARE $h_instance AS Utf8;

DELETE FROM `{ydb_locker_table}` ON
  SELECT object_id FROM `G2DEMO/BANK$WORK_LOCK` VIEW ix_owner
  WHERE h_type=$h_type AND h_instance=$h_instance;

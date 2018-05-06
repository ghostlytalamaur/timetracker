CREATE TRIGGER IF NOT EXISTS auto_append_day AFTER INSERT ON sessions FOR EACH ROW
BEGIN
	INSERT OR IGNORE INTO days(date)
		SELECT strftime('%s', NEW.StartTime, 'unixepoch', 'localtime')
		WHERE NOT EXISTS(
			SELECT 1 FROM days WHERE date(date, 'unixepoch', 'localtime') = date(NEW.StartTime, 'unixepoch', 'localtime')
		)
	;
	UPDATE sessions SET dayId = (
		SELECT _dayId FROM days WHERE date(date, 'unixepoch', 'localtime') = date(NEW.StartTime, 'unixepoch', 'localtime')
		)
	WHERE ID = NEW.ID
	;
END
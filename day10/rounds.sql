DROP DATABASE IF EXISTS day10;
CREATE DATABASE day10;
USE day10;

CREATE TABLE file(raw VARCHAR(16));
CREATE TABLE log(inst VARCHAR(8), add_val INT, cycle INT, value INT);
LOAD DATA INFILE '/Users/loicdiasdasilva/code/AoC-2022/day10/input.txt' INTO TABLE file;

INSERT INTO log
SELECT inst, add_val, 
       @cycle := @cycle + IF(inst = 'noop', 1, 2) as cycle, 
       @value := @value + add_val as value 
FROM (
	SELECT substring_index (raw,' ',1) as inst, 
	       IF(substring_index(raw,' ',-1) = 'noop', 0, 
		  convert(substring_index(raw,' ',-1), signed)) as add_val 
	FROM file
) t 
CROSS JOIN (SELECT @cycle := 0) c 
CROSS JOIN (select @value := 1) v
;

SELECT s_20 + s_60 + s_100 + s_140 + s_180 + s_220 as round_1 FROM (
  SELECT 20 * value AS s_20, s_60, s_100, s_140, s_180, s_220 FROM log   
  CROSS JOIN (SELECT 60 * value AS s_60 FROM log WHERE cycle < 60 ORDER BY cycle DESC LIMIT 1) t2
  CROSS JOIN (SELECT 100 * value AS s_100 FROM log WHERE cycle < 100 ORDER BY cycle DESC LIMIT 1) t3
  CROSS JOIN (SELECT 140 * value AS s_140 FROM log WHERE cycle < 140 ORDER BY cycle DESC LIMIT 1) t4
  CROSS JOIN (SELECT 180 * value AS s_180 FROM log WHERE cycle < 180 ORDER BY cycle DESC LIMIT 1) t5
  CROSS JOIN (SELECT 220 * value AS s_220 FROM log WHERE cycle < 220 ORDER BY cycle DESC LIMIT 1) t6
  WHERE cycle < 20 ORDER BY cycle DESC LIMIT 1
) t1;

CREATE TABLE pixels(cycle INT NOT NULL AUTO_INCREMENT, line INT, pixel CHAR, PRIMARY KEY(cycle));
DELIMITER | 
CREATE PROCEDURE crt() 
BEGIN 
	DECLARE c_max INT; 
	DECLARE c_index INT UNSIGNED DEFAULT 0; 
        DECLARE l_index INT UNSIGNED DEFAULT 1;

	SELECT max(cycle) INTO c_max FROM log; 
	WHILE c_index <= c_max DO 
		IF (EXISTS(SELECT value FROM log WHERE cycle < c_index + 1)) THEN
			INSERT INTO pixels(line, pixel)
			SELECT
				l_index,
				IF (MOD(c_index, 40) IN (value-1, value, value+1), "#", ".") as pixel
			FROM log
			WHERE cycle < c_index + 1
			ORDER BY cycle DESC
			LIMIT 1;
		ELSE
			INSERT INTO pixels(line, pixel) VALUES (
				l_index,
				IF (MOD(c_index, 40) IN (0, 1, 2), "#", ".")
			);
		END IF;
		
		SET l_index = IF(MOD(c_index + 1, 40) = 0 AND c_index >= 39, l_index + 1, l_index);
		SET c_index = c_index + 1; 
	END WHILE; 
END | 
DELIMITER ;
CALL crt();

SELECT GROUP_CONCAT(pixel) FROM pixels GROUP BY line;

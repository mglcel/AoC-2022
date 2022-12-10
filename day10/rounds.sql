DROP DATABASE IF EXISTS day10;
CREATE DATABASE day10;
CREATE TABLE day10.file(raw VARCHAR(16));
CREATE TABLE day10.log(inst VARCHAR(8), add_val INT, cycle INT, value INT);
LOAD DATA INFILE '/Users/loicdiasdasilva/code/AoC-2022/day10/input.txt' INTO TABLE day10.file;

INSERT INTO day10.log
SELECT inst, add_val, 
       @cycle := @cycle + IF(inst = 'noop', 1, 2) as cycle, 
       @value := @value + add_val as value 
FROM (
	SELECT substring_index (raw,' ',1) as inst, 
	       IF(substring_index(raw,' ',-1) = 'noop', 0, 
		  convert(substring_index(raw,' ',-1), signed)) as add_val 
	FROM day10.file
) t 
CROSS JOIN (SELECT @cycle := 0) c 
CROSS JOIN (select @value := 1) v
;

SELECT s_20 + s_60 + s_100 + s_140 + s_180 + s_220 as round_1 FROM (
  SELECT 20 * value AS s_20, s_60, s_100, s_140, s_180, s_220 FROM day10.log   
  CROSS JOIN (SELECT 60 * value AS s_60 FROM day10.log WHERE cycle < 60 ORDER BY cycle DESC LIMIT 1) t2
  CROSS JOIN (SELECT 100 * value AS s_100 FROM day10.log WHERE cycle < 100 ORDER BY cycle DESC LIMIT 1) t3
  CROSS JOIN (SELECT 140 * value AS s_140 FROM day10.log WHERE cycle < 140 ORDER BY cycle DESC LIMIT 1) t4
  CROSS JOIN (SELECT 180 * value AS s_180 FROM day10.log WHERE cycle < 180 ORDER BY cycle DESC LIMIT 1) t5
  CROSS JOIN (SELECT 220 * value AS s_220 FROM day10.log WHERE cycle < 220 ORDER BY cycle DESC LIMIT 1) t6
  WHERE cycle < 20 ORDER BY cycle DESC LIMIT 1
) t1;


column seq_count new_value next_num
select seq_count + 51 as seq_count from sequence;
CREATE SEQUENCE SEQ_GEN_SEQUENCE INCREMENT BY 50 START WITH &next_num;
exit
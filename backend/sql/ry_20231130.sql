-- ----------------------------
-- 1銆侀儴闂ㄨ〃
-- ----------------------------
drop table if exists sys_dept;
create table sys_dept (
  dept_id           bigint(20)      not null auto_increment    comment '閮ㄩ棬id',
  parent_id         bigint(20)      default 0                  comment '鐖堕儴闂╥d',
  ancestors         varchar(50)     default ''                 comment '绁栫骇鍒楄〃',
  dept_name         varchar(30)     default ''                 comment '閮ㄩ棬鍚嶇О',
  order_num         int(4)          default 0                  comment '鏄剧ず椤哄簭',
  leader            varchar(20)     default null               comment '璐熻矗浜?,
  phone             varchar(11)     default null               comment '鑱旂郴鐢佃瘽',
  email             varchar(50)     default null               comment '閭',
  status            char(1)         default '0'                comment '閮ㄩ棬鐘舵€侊紙0姝ｅ父 1鍋滅敤锛?,
  del_flag          char(1)         default '0'                comment '鍒犻櫎鏍囧織锛?浠ｈ〃瀛樺湪 2浠ｈ〃鍒犻櫎锛?,
  create_by         varchar(64)     default ''                 comment '鍒涘缓鑰?,
  create_time 	    datetime                                   comment '鍒涘缓鏃堕棿',
  update_by         varchar(64)     default ''                 comment '鏇存柊鑰?,
  update_time       datetime                                   comment '鏇存柊鏃堕棿',
  primary key (dept_id)
) engine=innodb auto_increment=200 comment = '閮ㄩ棬琛?;

-- ----------------------------
-- 鍒濆鍖?閮ㄩ棬琛ㄦ暟鎹?
-- ----------------------------
insert into sys_dept values(100,  0,   '0',          '鑻ヤ緷绉戞妧',   0, '鑻ヤ緷', '15888888888', 'ops@qkyd.cn', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(101,  100, '0,100',      '娣卞湷鎬诲叕鍙?, 1, '鑻ヤ緷', '15888888888', 'ops@qkyd.cn', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(102,  100, '0,100',      '闀挎矙鍒嗗叕鍙?, 2, '鑻ヤ緷', '15888888888', 'ops@qkyd.cn', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(103,  101, '0,100,101',  '鐮斿彂閮ㄩ棬',   1, '鑻ヤ緷', '15888888888', 'ops@qkyd.cn', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(104,  101, '0,100,101',  '甯傚満閮ㄩ棬',   2, '鑻ヤ緷', '15888888888', 'ops@qkyd.cn', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(105,  101, '0,100,101',  '娴嬭瘯閮ㄩ棬',   3, '鑻ヤ緷', '15888888888', 'ops@qkyd.cn', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(106,  101, '0,100,101',  '璐㈠姟閮ㄩ棬',   4, '鑻ヤ緷', '15888888888', 'ops@qkyd.cn', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(107,  101, '0,100,101',  '杩愮淮閮ㄩ棬',   5, '鑻ヤ緷', '15888888888', 'ops@qkyd.cn', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(108,  102, '0,100,102',  '甯傚満閮ㄩ棬',   1, '鑻ヤ緷', '15888888888', 'ops@qkyd.cn', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(109,  102, '0,100,102',  '璐㈠姟閮ㄩ棬',   2, '鑻ヤ緷', '15888888888', 'ops@qkyd.cn', '0', '0', 'admin', sysdate(), '', null);


-- ----------------------------
-- 2銆佺敤鎴蜂俊鎭〃
-- ----------------------------
drop table if exists sys_user;
create table sys_user (
  user_id           bigint(20)      not null auto_increment    comment '鐢ㄦ埛ID',
  dept_id           bigint(20)      default null               comment '閮ㄩ棬ID',
  user_name         varchar(30)     not null                   comment '鐢ㄦ埛璐﹀彿',
  nick_name         varchar(30)     not null                   comment '鐢ㄦ埛鏄电О',
  user_type         varchar(2)      default '00'               comment '鐢ㄦ埛绫诲瀷锛?0绯荤粺鐢ㄦ埛锛?,
  email             varchar(50)     default ''                 comment '鐢ㄦ埛閭',
  phonenumber       varchar(11)     default ''                 comment '鎵嬫満鍙风爜',
  sex               char(1)         default '0'                comment '鐢ㄦ埛鎬у埆锛?鐢?1濂?2鏈煡锛?,
  avatar            varchar(100)    default ''                 comment '澶村儚鍦板潃',
  password          varchar(100)    default ''                 comment '瀵嗙爜',
  status            char(1)         default '0'                comment '甯愬彿鐘舵€侊紙0姝ｅ父 1鍋滅敤锛?,
  del_flag          char(1)         default '0'                comment '鍒犻櫎鏍囧織锛?浠ｈ〃瀛樺湪 2浠ｈ〃鍒犻櫎锛?,
  login_ip          varchar(128)    default ''                 comment '鏈€鍚庣櫥褰旾P',
  login_date        datetime                                   comment '鏈€鍚庣櫥褰曟椂闂?,
  create_by         varchar(64)     default ''                 comment '鍒涘缓鑰?,
  create_time       datetime                                   comment '鍒涘缓鏃堕棿',
  update_by         varchar(64)     default ''                 comment '鏇存柊鑰?,
  update_time       datetime                                   comment '鏇存柊鏃堕棿',
  remark            varchar(500)    default null               comment '澶囨敞',
  primary key (user_id)
) engine=innodb auto_increment=100 comment = '鐢ㄦ埛淇℃伅琛?;

-- ----------------------------
-- 鍒濆鍖?鐢ㄦ埛淇℃伅琛ㄦ暟鎹?
-- ----------------------------
insert into sys_user values(1,  103, 'admin', '鑻ヤ緷', '00', 'admin@qkyd.cn', '15888888888', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), 'admin', sysdate(), '', null, '绠＄悊鍛?);
insert into sys_user values(2,  105, 'ry',    '鑻ヤ緷', '00', 'ops@qkyd.cn',  '15666666666', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), 'admin', sysdate(), '', null, '娴嬭瘯鍛?);


-- ----------------------------
-- 3銆佸矖浣嶄俊鎭〃
-- ----------------------------
drop table if exists sys_post;
create table sys_post
(
  post_id       bigint(20)      not null auto_increment    comment '宀椾綅ID',
  post_code     varchar(64)     not null                   comment '宀椾綅缂栫爜',
  post_name     varchar(50)     not null                   comment '宀椾綅鍚嶇О',
  post_sort     int(4)          not null                   comment '鏄剧ず椤哄簭',
  status        char(1)         not null                   comment '鐘舵€侊紙0姝ｅ父 1鍋滅敤锛?,
  create_by     varchar(64)     default ''                 comment '鍒涘缓鑰?,
  create_time   datetime                                   comment '鍒涘缓鏃堕棿',
  update_by     varchar(64)     default ''			       comment '鏇存柊鑰?,
  update_time   datetime                                   comment '鏇存柊鏃堕棿',
  remark        varchar(500)    default null               comment '澶囨敞',
  primary key (post_id)
) engine=innodb comment = '宀椾綅淇℃伅琛?;

-- ----------------------------
-- 鍒濆鍖?宀椾綅淇℃伅琛ㄦ暟鎹?
-- ----------------------------
insert into sys_post values(1, 'ceo',  '钁ｄ簨闀?,    1, '0', 'admin', sysdate(), '', null, '');
insert into sys_post values(2, 'se',   '椤圭洰缁忕悊',  2, '0', 'admin', sysdate(), '', null, '');
insert into sys_post values(3, 'hr',   '浜哄姏璧勬簮',  3, '0', 'admin', sysdate(), '', null, '');
insert into sys_post values(4, 'user', '鏅€氬憳宸?,  4, '0', 'admin', sysdate(), '', null, '');


-- ----------------------------
-- 4銆佽鑹蹭俊鎭〃
-- ----------------------------
drop table if exists sys_role;
create table sys_role (
  role_id              bigint(20)      not null auto_increment    comment '瑙掕壊ID',
  role_name            varchar(30)     not null                   comment '瑙掕壊鍚嶇О',
  role_key             varchar(100)    not null                   comment '瑙掕壊鏉冮檺瀛楃涓?,
  role_sort            int(4)          not null                   comment '鏄剧ず椤哄簭',
  data_scope           char(1)         default '1'                comment '鏁版嵁鑼冨洿锛?锛氬叏閮ㄦ暟鎹潈闄?2锛氳嚜瀹氭暟鎹潈闄?3锛氭湰閮ㄩ棬鏁版嵁鏉冮檺 4锛氭湰閮ㄩ棬鍙婁互涓嬫暟鎹潈闄愶級',
  menu_check_strictly  tinyint(1)      default 1                  comment '鑿滃崟鏍戦€夋嫨椤规槸鍚﹀叧鑱旀樉绀?,
  dept_check_strictly  tinyint(1)      default 1                  comment '閮ㄩ棬鏍戦€夋嫨椤规槸鍚﹀叧鑱旀樉绀?,
  status               char(1)         not null                   comment '瑙掕壊鐘舵€侊紙0姝ｅ父 1鍋滅敤锛?,
  del_flag             char(1)         default '0'                comment '鍒犻櫎鏍囧織锛?浠ｈ〃瀛樺湪 2浠ｈ〃鍒犻櫎锛?,
  create_by            varchar(64)     default ''                 comment '鍒涘缓鑰?,
  create_time          datetime                                   comment '鍒涘缓鏃堕棿',
  update_by            varchar(64)     default ''                 comment '鏇存柊鑰?,
  update_time          datetime                                   comment '鏇存柊鏃堕棿',
  remark               varchar(500)    default null               comment '澶囨敞',
  primary key (role_id)
) engine=innodb auto_increment=100 comment = '瑙掕壊淇℃伅琛?;

-- ----------------------------
-- 鍒濆鍖?瑙掕壊淇℃伅琛ㄦ暟鎹?
-- ----------------------------
insert into sys_role values('1', '瓒呯骇绠＄悊鍛?,  'admin',  1, 1, 1, 1, '0', '0', 'admin', sysdate(), '', null, '瓒呯骇绠＄悊鍛?);
insert into sys_role values('2', '鏅€氳鑹?,    'common', 2, 2, 1, 1, '0', '0', 'admin', sysdate(), '', null, '鏅€氳鑹?);


-- ----------------------------
-- 5銆佽彍鍗曟潈闄愯〃
-- ----------------------------
drop table if exists sys_menu;
create table sys_menu (
  menu_id           bigint(20)      not null auto_increment    comment '鑿滃崟ID',
  menu_name         varchar(50)     not null                   comment '鑿滃崟鍚嶇О',
  parent_id         bigint(20)      default 0                  comment '鐖惰彍鍗旾D',
  order_num         int(4)          default 0                  comment '鏄剧ず椤哄簭',
  path              varchar(200)    default ''                 comment '璺敱鍦板潃',
  component         varchar(255)    default null               comment '缁勪欢璺緞',
  query             varchar(255)    default null               comment '璺敱鍙傛暟',
  is_frame          int(1)          default 1                  comment '鏄惁涓哄閾撅紙0鏄?1鍚︼級',
  is_cache          int(1)          default 0                  comment '鏄惁缂撳瓨锛?缂撳瓨 1涓嶇紦瀛橈級',
  menu_type         char(1)         default ''                 comment '鑿滃崟绫诲瀷锛圡鐩綍 C鑿滃崟 F鎸夐挳锛?,
  visible           char(1)         default 0                  comment '鑿滃崟鐘舵€侊紙0鏄剧ず 1闅愯棌锛?,
  status            char(1)         default 0                  comment '鑿滃崟鐘舵€侊紙0姝ｅ父 1鍋滅敤锛?,
  perms             varchar(100)    default null               comment '鏉冮檺鏍囪瘑',
  icon              varchar(100)    default '#'                comment '鑿滃崟鍥炬爣',
  create_by         varchar(64)     default ''                 comment '鍒涘缓鑰?,
  create_time       datetime                                   comment '鍒涘缓鏃堕棿',
  update_by         varchar(64)     default ''                 comment '鏇存柊鑰?,
  update_time       datetime                                   comment '鏇存柊鏃堕棿',
  remark            varchar(500)    default ''                 comment '澶囨敞',
  primary key (menu_id)
) engine=innodb auto_increment=2000 comment = '鑿滃崟鏉冮檺琛?;

-- ----------------------------
-- 鍒濆鍖?鑿滃崟淇℃伅琛ㄦ暟鎹?
-- ----------------------------
-- 涓€绾ц彍鍗?
insert into sys_menu values('1', '绯荤粺绠＄悊', '0', '1', 'system',           null, '', 1, 0, 'M', '0', '0', '', 'system',   'admin', sysdate(), '', null, '绯荤粺绠＄悊鐩綍');
insert into sys_menu values('2', '绯荤粺鐩戞帶', '0', '2', 'monitor',          null, '', 1, 0, 'M', '0', '0', '', 'monitor',  'admin', sysdate(), '', null, '绯荤粺鐩戞帶鐩綍');
insert into sys_menu values('3', '绯荤粺宸ュ叿', '0', '3', 'tool',             null, '', 1, 0, 'M', '0', '0', '', 'tool',     'admin', sysdate(), '', null, '绯荤粺宸ュ叿鐩綍');
insert into sys_menu values('4', '鑻ヤ緷瀹樼綉', '0', '4', 'https://www.qkyd.cn', null, '', 0, 0, 'M', '0', '0', '', 'guide',    'admin', sysdate(), '', null, '鑻ヤ緷瀹樼綉鍦板潃');
-- 浜岀骇鑿滃崟
insert into sys_menu values('100',  '鐢ㄦ埛绠＄悊', '1',   '1', 'user',       'system/user/index',        '', 1, 0, 'C', '0', '0', 'system:user:list',        'user',          'admin', sysdate(), '', null, '鐢ㄦ埛绠＄悊鑿滃崟');
insert into sys_menu values('101',  '瑙掕壊绠＄悊', '1',   '2', 'role',       'system/role/index',        '', 1, 0, 'C', '0', '0', 'system:role:list',        'peoples',       'admin', sysdate(), '', null, '瑙掕壊绠＄悊鑿滃崟');
insert into sys_menu values('102',  '鑿滃崟绠＄悊', '1',   '3', 'menu',       'system/menu/index',        '', 1, 0, 'C', '0', '0', 'system:menu:list',        'tree-table',    'admin', sysdate(), '', null, '鑿滃崟绠＄悊鑿滃崟');
insert into sys_menu values('103',  '閮ㄩ棬绠＄悊', '1',   '4', 'dept',       'system/dept/index',        '', 1, 0, 'C', '0', '0', 'system:dept:list',        'tree',          'admin', sysdate(), '', null, '閮ㄩ棬绠＄悊鑿滃崟');
insert into sys_menu values('104',  '宀椾綅绠＄悊', '1',   '5', 'post',       'system/post/index',        '', 1, 0, 'C', '0', '0', 'system:post:list',        'post',          'admin', sysdate(), '', null, '宀椾綅绠＄悊鑿滃崟');
insert into sys_menu values('105',  '瀛楀吀绠＄悊', '1',   '6', 'dict',       'system/dict/index',        '', 1, 0, 'C', '0', '0', 'system:dict:list',        'dict',          'admin', sysdate(), '', null, '瀛楀吀绠＄悊鑿滃崟');
insert into sys_menu values('106',  '鍙傛暟璁剧疆', '1',   '7', 'config',     'system/config/index',      '', 1, 0, 'C', '0', '0', 'system:config:list',      'edit',          'admin', sysdate(), '', null, '鍙傛暟璁剧疆鑿滃崟');
insert into sys_menu values('107',  '閫氱煡鍏憡', '1',   '8', 'notice',     'system/notice/index',      '', 1, 0, 'C', '0', '0', 'system:notice:list',      'message',       'admin', sysdate(), '', null, '閫氱煡鍏憡鑿滃崟');
insert into sys_menu values('108',  '鏃ュ織绠＄悊', '1',   '9', 'log',        '',                         '', 1, 0, 'M', '0', '0', '',                        'log',           'admin', sysdate(), '', null, '鏃ュ織绠＄悊鑿滃崟');
insert into sys_menu values('109',  '鍦ㄧ嚎鐢ㄦ埛', '2',   '1', 'online',     'monitor/online/index',     '', 1, 0, 'C', '0', '0', 'monitor:online:list',     'online',        'admin', sysdate(), '', null, '鍦ㄧ嚎鐢ㄦ埛鑿滃崟');
insert into sys_menu values('110',  '瀹氭椂浠诲姟', '2',   '2', 'job',        'monitor/job/index',        '', 1, 0, 'C', '0', '0', 'monitor:job:list',        'job',           'admin', sysdate(), '', null, '瀹氭椂浠诲姟鑿滃崟');
insert into sys_menu values('111',  '鏁版嵁鐩戞帶', '2',   '3', 'druid',      'monitor/druid/index',      '', 1, 0, 'C', '0', '0', 'monitor:druid:list',      'druid',         'admin', sysdate(), '', null, '鏁版嵁鐩戞帶鑿滃崟');
insert into sys_menu values('112',  '鏈嶅姟鐩戞帶', '2',   '4', 'server',     'monitor/server/index',     '', 1, 0, 'C', '0', '0', 'monitor:server:list',     'server',        'admin', sysdate(), '', null, '鏈嶅姟鐩戞帶鑿滃崟');
insert into sys_menu values('113',  '缂撳瓨鐩戞帶', '2',   '5', 'cache',      'monitor/cache/index',      '', 1, 0, 'C', '0', '0', 'monitor:cache:list',      'redis',         'admin', sysdate(), '', null, '缂撳瓨鐩戞帶鑿滃崟');
insert into sys_menu values('114',  '缂撳瓨鍒楄〃', '2',   '6', 'cacheList',  'monitor/cache/list',       '', 1, 0, 'C', '0', '0', 'monitor:cache:list',      'redis-list',    'admin', sysdate(), '', null, '缂撳瓨鍒楄〃鑿滃崟');
insert into sys_menu values('115',  '琛ㄥ崟鏋勫缓', '3',   '1', 'build',      'tool/build/index',         '', 1, 0, 'C', '0', '0', 'tool:build:list',         'build',         'admin', sysdate(), '', null, '琛ㄥ崟鏋勫缓鑿滃崟');
insert into sys_menu values('116',  '浠ｇ爜鐢熸垚', '3',   '2', 'gen',        'tool/gen/index',           '', 1, 0, 'C', '0', '0', 'tool:gen:list',           'code',          'admin', sysdate(), '', null, '浠ｇ爜鐢熸垚鑿滃崟');
insert into sys_menu values('117',  '绯荤粺鎺ュ彛', '3',   '3', 'swagger',    'tool/swagger/index',       '', 1, 0, 'C', '0', '0', 'tool:swagger:list',       'swagger',       'admin', sysdate(), '', null, '绯荤粺鎺ュ彛鑿滃崟');
-- 涓夌骇鑿滃崟
insert into sys_menu values('500',  '鎿嶄綔鏃ュ織', '108', '1', 'operlog',    'monitor/operlog/index',    '', 1, 0, 'C', '0', '0', 'monitor:operlog:list',    'form',          'admin', sysdate(), '', null, '鎿嶄綔鏃ュ織鑿滃崟');
insert into sys_menu values('501',  '鐧诲綍鏃ュ織', '108', '2', 'logininfor', 'monitor/logininfor/index', '', 1, 0, 'C', '0', '0', 'monitor:logininfor:list', 'logininfor',    'admin', sysdate(), '', null, '鐧诲綍鏃ュ織鑿滃崟');
-- 鐢ㄦ埛绠＄悊鎸夐挳
insert into sys_menu values('1000', '鐢ㄦ埛鏌ヨ', '100', '1',  '', '', '', 1, 0, 'F', '0', '0', 'system:user:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1001', '鐢ㄦ埛鏂板', '100', '2',  '', '', '', 1, 0, 'F', '0', '0', 'system:user:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1002', '鐢ㄦ埛淇敼', '100', '3',  '', '', '', 1, 0, 'F', '0', '0', 'system:user:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1003', '鐢ㄦ埛鍒犻櫎', '100', '4',  '', '', '', 1, 0, 'F', '0', '0', 'system:user:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1004', '鐢ㄦ埛瀵煎嚭', '100', '5',  '', '', '', 1, 0, 'F', '0', '0', 'system:user:export',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1005', '鐢ㄦ埛瀵煎叆', '100', '6',  '', '', '', 1, 0, 'F', '0', '0', 'system:user:import',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1006', '閲嶇疆瀵嗙爜', '100', '7',  '', '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd',       '#', 'admin', sysdate(), '', null, '');
-- 瑙掕壊绠＄悊鎸夐挳
insert into sys_menu values('1007', '瑙掕壊鏌ヨ', '101', '1',  '', '', '', 1, 0, 'F', '0', '0', 'system:role:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1008', '瑙掕壊鏂板', '101', '2',  '', '', '', 1, 0, 'F', '0', '0', 'system:role:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1009', '瑙掕壊淇敼', '101', '3',  '', '', '', 1, 0, 'F', '0', '0', 'system:role:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1010', '瑙掕壊鍒犻櫎', '101', '4',  '', '', '', 1, 0, 'F', '0', '0', 'system:role:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1011', '瑙掕壊瀵煎嚭', '101', '5',  '', '', '', 1, 0, 'F', '0', '0', 'system:role:export',         '#', 'admin', sysdate(), '', null, '');
-- 鑿滃崟绠＄悊鎸夐挳
insert into sys_menu values('1012', '鑿滃崟鏌ヨ', '102', '1',  '', '', '', 1, 0, 'F', '0', '0', 'system:menu:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1013', '鑿滃崟鏂板', '102', '2',  '', '', '', 1, 0, 'F', '0', '0', 'system:menu:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1014', '鑿滃崟淇敼', '102', '3',  '', '', '', 1, 0, 'F', '0', '0', 'system:menu:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1015', '鑿滃崟鍒犻櫎', '102', '4',  '', '', '', 1, 0, 'F', '0', '0', 'system:menu:remove',         '#', 'admin', sysdate(), '', null, '');
-- 閮ㄩ棬绠＄悊鎸夐挳
insert into sys_menu values('1016', '閮ㄩ棬鏌ヨ', '103', '1',  '', '', '', 1, 0, 'F', '0', '0', 'system:dept:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1017', '閮ㄩ棬鏂板', '103', '2',  '', '', '', 1, 0, 'F', '0', '0', 'system:dept:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1018', '閮ㄩ棬淇敼', '103', '3',  '', '', '', 1, 0, 'F', '0', '0', 'system:dept:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1019', '閮ㄩ棬鍒犻櫎', '103', '4',  '', '', '', 1, 0, 'F', '0', '0', 'system:dept:remove',         '#', 'admin', sysdate(), '', null, '');
-- 宀椾綅绠＄悊鎸夐挳
insert into sys_menu values('1020', '宀椾綅鏌ヨ', '104', '1',  '', '', '', 1, 0, 'F', '0', '0', 'system:post:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1021', '宀椾綅鏂板', '104', '2',  '', '', '', 1, 0, 'F', '0', '0', 'system:post:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1022', '宀椾綅淇敼', '104', '3',  '', '', '', 1, 0, 'F', '0', '0', 'system:post:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1023', '宀椾綅鍒犻櫎', '104', '4',  '', '', '', 1, 0, 'F', '0', '0', 'system:post:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1024', '宀椾綅瀵煎嚭', '104', '5',  '', '', '', 1, 0, 'F', '0', '0', 'system:post:export',         '#', 'admin', sysdate(), '', null, '');
-- 瀛楀吀绠＄悊鎸夐挳
insert into sys_menu values('1025', '瀛楀吀鏌ヨ', '105', '1', '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1026', '瀛楀吀鏂板', '105', '2', '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1027', '瀛楀吀淇敼', '105', '3', '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1028', '瀛楀吀鍒犻櫎', '105', '4', '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1029', '瀛楀吀瀵煎嚭', '105', '5', '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:export',         '#', 'admin', sysdate(), '', null, '');
-- 鍙傛暟璁剧疆鎸夐挳
insert into sys_menu values('1030', '鍙傛暟鏌ヨ', '106', '1', '#', '', '', 1, 0, 'F', '0', '0', 'system:config:query',        '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1031', '鍙傛暟鏂板', '106', '2', '#', '', '', 1, 0, 'F', '0', '0', 'system:config:add',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1032', '鍙傛暟淇敼', '106', '3', '#', '', '', 1, 0, 'F', '0', '0', 'system:config:edit',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1033', '鍙傛暟鍒犻櫎', '106', '4', '#', '', '', 1, 0, 'F', '0', '0', 'system:config:remove',       '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1034', '鍙傛暟瀵煎嚭', '106', '5', '#', '', '', 1, 0, 'F', '0', '0', 'system:config:export',       '#', 'admin', sysdate(), '', null, '');
-- 閫氱煡鍏憡鎸夐挳
insert into sys_menu values('1035', '鍏憡鏌ヨ', '107', '1', '#', '', '', 1, 0, 'F', '0', '0', 'system:notice:query',        '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1036', '鍏憡鏂板', '107', '2', '#', '', '', 1, 0, 'F', '0', '0', 'system:notice:add',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1037', '鍏憡淇敼', '107', '3', '#', '', '', 1, 0, 'F', '0', '0', 'system:notice:edit',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1038', '鍏憡鍒犻櫎', '107', '4', '#', '', '', 1, 0, 'F', '0', '0', 'system:notice:remove',       '#', 'admin', sysdate(), '', null, '');
-- 鎿嶄綔鏃ュ織鎸夐挳
insert into sys_menu values('1039', '鎿嶄綔鏌ヨ', '500', '1', '#', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:query',      '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1040', '鎿嶄綔鍒犻櫎', '500', '2', '#', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:remove',     '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1041', '鏃ュ織瀵煎嚭', '500', '3', '#', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:export',     '#', 'admin', sysdate(), '', null, '');
-- 鐧诲綍鏃ュ織鎸夐挳
insert into sys_menu values('1042', '鐧诲綍鏌ヨ', '501', '1', '#', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:query',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1043', '鐧诲綍鍒犻櫎', '501', '2', '#', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:remove',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1044', '鏃ュ織瀵煎嚭', '501', '3', '#', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:export',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1045', '璐︽埛瑙ｉ攣', '501', '4', '#', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:unlock',  '#', 'admin', sysdate(), '', null, '');
-- 鍦ㄧ嚎鐢ㄦ埛鎸夐挳
insert into sys_menu values('1046', '鍦ㄧ嚎鏌ヨ', '109', '1', '#', '', '', 1, 0, 'F', '0', '0', 'monitor:online:query',       '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1047', '鎵归噺寮洪€€', '109', '2', '#', '', '', 1, 0, 'F', '0', '0', 'monitor:online:batchLogout', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1048', '鍗曟潯寮洪€€', '109', '3', '#', '', '', 1, 0, 'F', '0', '0', 'monitor:online:forceLogout', '#', 'admin', sysdate(), '', null, '');
-- 瀹氭椂浠诲姟鎸夐挳
insert into sys_menu values('1049', '浠诲姟鏌ヨ', '110', '1', '#', '', '', 1, 0, 'F', '0', '0', 'monitor:job:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1050', '浠诲姟鏂板', '110', '2', '#', '', '', 1, 0, 'F', '0', '0', 'monitor:job:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1051', '浠诲姟淇敼', '110', '3', '#', '', '', 1, 0, 'F', '0', '0', 'monitor:job:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1052', '浠诲姟鍒犻櫎', '110', '4', '#', '', '', 1, 0, 'F', '0', '0', 'monitor:job:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1053', '鐘舵€佷慨鏀?, '110', '5', '#', '', '', 1, 0, 'F', '0', '0', 'monitor:job:changeStatus',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1054', '浠诲姟瀵煎嚭', '110', '6', '#', '', '', 1, 0, 'F', '0', '0', 'monitor:job:export',         '#', 'admin', sysdate(), '', null, '');
-- 浠ｇ爜鐢熸垚鎸夐挳
insert into sys_menu values('1055', '鐢熸垚鏌ヨ', '116', '1', '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:query',             '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1056', '鐢熸垚淇敼', '116', '2', '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:edit',              '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1057', '鐢熸垚鍒犻櫎', '116', '3', '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:remove',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1058', '瀵煎叆浠ｇ爜', '116', '4', '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:import',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1059', '棰勮浠ｇ爜', '116', '5', '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:preview',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1060', '鐢熸垚浠ｇ爜', '116', '6', '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:code',              '#', 'admin', sysdate(), '', null, '');


-- ----------------------------
-- 6銆佺敤鎴峰拰瑙掕壊鍏宠仈琛? 鐢ㄦ埛N-1瑙掕壊
-- ----------------------------
drop table if exists sys_user_role;
create table sys_user_role (
  user_id   bigint(20) not null comment '鐢ㄦ埛ID',
  role_id   bigint(20) not null comment '瑙掕壊ID',
  primary key(user_id, role_id)
) engine=innodb comment = '鐢ㄦ埛鍜岃鑹插叧鑱旇〃';

-- ----------------------------
-- 鍒濆鍖?鐢ㄦ埛鍜岃鑹插叧鑱旇〃鏁版嵁
-- ----------------------------
insert into sys_user_role values ('1', '1');
insert into sys_user_role values ('2', '2');


-- ----------------------------
-- 7銆佽鑹插拰鑿滃崟鍏宠仈琛? 瑙掕壊1-N鑿滃崟
-- ----------------------------
drop table if exists sys_role_menu;
create table sys_role_menu (
  role_id   bigint(20) not null comment '瑙掕壊ID',
  menu_id   bigint(20) not null comment '鑿滃崟ID',
  primary key(role_id, menu_id)
) engine=innodb comment = '瑙掕壊鍜岃彍鍗曞叧鑱旇〃';

-- ----------------------------
-- 鍒濆鍖?瑙掕壊鍜岃彍鍗曞叧鑱旇〃鏁版嵁
-- ----------------------------
insert into sys_role_menu values ('2', '1');
insert into sys_role_menu values ('2', '2');
insert into sys_role_menu values ('2', '3');
insert into sys_role_menu values ('2', '4');
insert into sys_role_menu values ('2', '100');
insert into sys_role_menu values ('2', '101');
insert into sys_role_menu values ('2', '102');
insert into sys_role_menu values ('2', '103');
insert into sys_role_menu values ('2', '104');
insert into sys_role_menu values ('2', '105');
insert into sys_role_menu values ('2', '106');
insert into sys_role_menu values ('2', '107');
insert into sys_role_menu values ('2', '108');
insert into sys_role_menu values ('2', '109');
insert into sys_role_menu values ('2', '110');
insert into sys_role_menu values ('2', '111');
insert into sys_role_menu values ('2', '112');
insert into sys_role_menu values ('2', '113');
insert into sys_role_menu values ('2', '114');
insert into sys_role_menu values ('2', '115');
insert into sys_role_menu values ('2', '116');
insert into sys_role_menu values ('2', '117');
insert into sys_role_menu values ('2', '500');
insert into sys_role_menu values ('2', '501');
insert into sys_role_menu values ('2', '1000');
insert into sys_role_menu values ('2', '1001');
insert into sys_role_menu values ('2', '1002');
insert into sys_role_menu values ('2', '1003');
insert into sys_role_menu values ('2', '1004');
insert into sys_role_menu values ('2', '1005');
insert into sys_role_menu values ('2', '1006');
insert into sys_role_menu values ('2', '1007');
insert into sys_role_menu values ('2', '1008');
insert into sys_role_menu values ('2', '1009');
insert into sys_role_menu values ('2', '1010');
insert into sys_role_menu values ('2', '1011');
insert into sys_role_menu values ('2', '1012');
insert into sys_role_menu values ('2', '1013');
insert into sys_role_menu values ('2', '1014');
insert into sys_role_menu values ('2', '1015');
insert into sys_role_menu values ('2', '1016');
insert into sys_role_menu values ('2', '1017');
insert into sys_role_menu values ('2', '1018');
insert into sys_role_menu values ('2', '1019');
insert into sys_role_menu values ('2', '1020');
insert into sys_role_menu values ('2', '1021');
insert into sys_role_menu values ('2', '1022');
insert into sys_role_menu values ('2', '1023');
insert into sys_role_menu values ('2', '1024');
insert into sys_role_menu values ('2', '1025');
insert into sys_role_menu values ('2', '1026');
insert into sys_role_menu values ('2', '1027');
insert into sys_role_menu values ('2', '1028');
insert into sys_role_menu values ('2', '1029');
insert into sys_role_menu values ('2', '1030');
insert into sys_role_menu values ('2', '1031');
insert into sys_role_menu values ('2', '1032');
insert into sys_role_menu values ('2', '1033');
insert into sys_role_menu values ('2', '1034');
insert into sys_role_menu values ('2', '1035');
insert into sys_role_menu values ('2', '1036');
insert into sys_role_menu values ('2', '1037');
insert into sys_role_menu values ('2', '1038');
insert into sys_role_menu values ('2', '1039');
insert into sys_role_menu values ('2', '1040');
insert into sys_role_menu values ('2', '1041');
insert into sys_role_menu values ('2', '1042');
insert into sys_role_menu values ('2', '1043');
insert into sys_role_menu values ('2', '1044');
insert into sys_role_menu values ('2', '1045');
insert into sys_role_menu values ('2', '1046');
insert into sys_role_menu values ('2', '1047');
insert into sys_role_menu values ('2', '1048');
insert into sys_role_menu values ('2', '1049');
insert into sys_role_menu values ('2', '1050');
insert into sys_role_menu values ('2', '1051');
insert into sys_role_menu values ('2', '1052');
insert into sys_role_menu values ('2', '1053');
insert into sys_role_menu values ('2', '1054');
insert into sys_role_menu values ('2', '1055');
insert into sys_role_menu values ('2', '1056');
insert into sys_role_menu values ('2', '1057');
insert into sys_role_menu values ('2', '1058');
insert into sys_role_menu values ('2', '1059');
insert into sys_role_menu values ('2', '1060');

-- ----------------------------
-- 8銆佽鑹插拰閮ㄩ棬鍏宠仈琛? 瑙掕壊1-N閮ㄩ棬
-- ----------------------------
drop table if exists sys_role_dept;
create table sys_role_dept (
  role_id   bigint(20) not null comment '瑙掕壊ID',
  dept_id   bigint(20) not null comment '閮ㄩ棬ID',
  primary key(role_id, dept_id)
) engine=innodb comment = '瑙掕壊鍜岄儴闂ㄥ叧鑱旇〃';

-- ----------------------------
-- 鍒濆鍖?瑙掕壊鍜岄儴闂ㄥ叧鑱旇〃鏁版嵁
-- ----------------------------
insert into sys_role_dept values ('2', '100');
insert into sys_role_dept values ('2', '101');
insert into sys_role_dept values ('2', '105');


-- ----------------------------
-- 9銆佺敤鎴蜂笌宀椾綅鍏宠仈琛? 鐢ㄦ埛1-N宀椾綅
-- ----------------------------
drop table if exists sys_user_post;
create table sys_user_post
(
  user_id   bigint(20) not null comment '鐢ㄦ埛ID',
  post_id   bigint(20) not null comment '宀椾綅ID',
  primary key (user_id, post_id)
) engine=innodb comment = '鐢ㄦ埛涓庡矖浣嶅叧鑱旇〃';

-- ----------------------------
-- 鍒濆鍖?鐢ㄦ埛涓庡矖浣嶅叧鑱旇〃鏁版嵁
-- ----------------------------
insert into sys_user_post values ('1', '1');
insert into sys_user_post values ('2', '2');


-- ----------------------------
-- 10銆佹搷浣滄棩蹇楄褰?
-- ----------------------------
drop table if exists sys_oper_log;
create table sys_oper_log (
  oper_id           bigint(20)      not null auto_increment    comment '鏃ュ織涓婚敭',
  title             varchar(50)     default ''                 comment '妯″潡鏍囬',
  business_type     int(2)          default 0                  comment '涓氬姟绫诲瀷锛?鍏跺畠 1鏂板 2淇敼 3鍒犻櫎锛?,
  method            varchar(100)    default ''                 comment '鏂规硶鍚嶇О',
  request_method    varchar(10)     default ''                 comment '璇锋眰鏂瑰紡',
  operator_type     int(1)          default 0                  comment '鎿嶄綔绫诲埆锛?鍏跺畠 1鍚庡彴鐢ㄦ埛 2鎵嬫満绔敤鎴凤級',
  oper_name         varchar(50)     default ''                 comment '鎿嶄綔浜哄憳',
  dept_name         varchar(50)     default ''                 comment '閮ㄩ棬鍚嶇О',
  oper_url          varchar(255)    default ''                 comment '璇锋眰URL',
  oper_ip           varchar(128)    default ''                 comment '涓绘満鍦板潃',
  oper_location     varchar(255)    default ''                 comment '鎿嶄綔鍦扮偣',
  oper_param        varchar(2000)   default ''                 comment '璇锋眰鍙傛暟',
  json_result       varchar(2000)   default ''                 comment '杩斿洖鍙傛暟',
  status            int(1)          default 0                  comment '鎿嶄綔鐘舵€侊紙0姝ｅ父 1寮傚父锛?,
  error_msg         varchar(2000)   default ''                 comment '閿欒娑堟伅',
  oper_time         datetime                                   comment '鎿嶄綔鏃堕棿',
  cost_time         bigint(20)      default 0                  comment '娑堣€楁椂闂?,
  primary key (oper_id),
  key idx_sys_oper_log_bt (business_type),
  key idx_sys_oper_log_s  (status),
  key idx_sys_oper_log_ot (oper_time)
) engine=innodb auto_increment=100 comment = '鎿嶄綔鏃ュ織璁板綍';


-- ----------------------------
-- 11銆佸瓧鍏哥被鍨嬭〃
-- ----------------------------
drop table if exists sys_dict_type;
create table sys_dict_type
(
  dict_id          bigint(20)      not null auto_increment    comment '瀛楀吀涓婚敭',
  dict_name        varchar(100)    default ''                 comment '瀛楀吀鍚嶇О',
  dict_type        varchar(100)    default ''                 comment '瀛楀吀绫诲瀷',
  status           char(1)         default '0'                comment '鐘舵€侊紙0姝ｅ父 1鍋滅敤锛?,
  create_by        varchar(64)     default ''                 comment '鍒涘缓鑰?,
  create_time      datetime                                   comment '鍒涘缓鏃堕棿',
  update_by        varchar(64)     default ''                 comment '鏇存柊鑰?,
  update_time      datetime                                   comment '鏇存柊鏃堕棿',
  remark           varchar(500)    default null               comment '澶囨敞',
  primary key (dict_id),
  unique (dict_type)
) engine=innodb auto_increment=100 comment = '瀛楀吀绫诲瀷琛?;

insert into sys_dict_type values(1,  '鐢ㄦ埛鎬у埆', 'sys_user_sex',        '0', 'admin', sysdate(), '', null, '鐢ㄦ埛鎬у埆鍒楄〃');
insert into sys_dict_type values(2,  '鑿滃崟鐘舵€?, 'sys_show_hide',       '0', 'admin', sysdate(), '', null, '鑿滃崟鐘舵€佸垪琛?);
insert into sys_dict_type values(3,  '绯荤粺寮€鍏?, 'sys_normal_disable',  '0', 'admin', sysdate(), '', null, '绯荤粺寮€鍏冲垪琛?);
insert into sys_dict_type values(4,  '浠诲姟鐘舵€?, 'sys_job_status',      '0', 'admin', sysdate(), '', null, '浠诲姟鐘舵€佸垪琛?);
insert into sys_dict_type values(5,  '浠诲姟鍒嗙粍', 'sys_job_group',       '0', 'admin', sysdate(), '', null, '浠诲姟鍒嗙粍鍒楄〃');
insert into sys_dict_type values(6,  '绯荤粺鏄惁', 'sys_yes_no',          '0', 'admin', sysdate(), '', null, '绯荤粺鏄惁鍒楄〃');
insert into sys_dict_type values(7,  '閫氱煡绫诲瀷', 'sys_notice_type',     '0', 'admin', sysdate(), '', null, '閫氱煡绫诲瀷鍒楄〃');
insert into sys_dict_type values(8,  '閫氱煡鐘舵€?, 'sys_notice_status',   '0', 'admin', sysdate(), '', null, '閫氱煡鐘舵€佸垪琛?);
insert into sys_dict_type values(9,  '鎿嶄綔绫诲瀷', 'sys_oper_type',       '0', 'admin', sysdate(), '', null, '鎿嶄綔绫诲瀷鍒楄〃');
insert into sys_dict_type values(10, '绯荤粺鐘舵€?, 'sys_common_status',   '0', 'admin', sysdate(), '', null, '鐧诲綍鐘舵€佸垪琛?);


-- ----------------------------
-- 12銆佸瓧鍏告暟鎹〃
-- ----------------------------
drop table if exists sys_dict_data;
create table sys_dict_data
(
  dict_code        bigint(20)      not null auto_increment    comment '瀛楀吀缂栫爜',
  dict_sort        int(4)          default 0                  comment '瀛楀吀鎺掑簭',
  dict_label       varchar(100)    default ''                 comment '瀛楀吀鏍囩',
  dict_value       varchar(100)    default ''                 comment '瀛楀吀閿€?,
  dict_type        varchar(100)    default ''                 comment '瀛楀吀绫诲瀷',
  css_class        varchar(100)    default null               comment '鏍峰紡灞炴€э紙鍏朵粬鏍峰紡鎵╁睍锛?,
  list_class       varchar(100)    default null               comment '琛ㄦ牸鍥炴樉鏍峰紡',
  is_default       char(1)         default 'N'                comment '鏄惁榛樿锛圷鏄?N鍚︼級',
  status           char(1)         default '0'                comment '鐘舵€侊紙0姝ｅ父 1鍋滅敤锛?,
  create_by        varchar(64)     default ''                 comment '鍒涘缓鑰?,
  create_time      datetime                                   comment '鍒涘缓鏃堕棿',
  update_by        varchar(64)     default ''                 comment '鏇存柊鑰?,
  update_time      datetime                                   comment '鏇存柊鏃堕棿',
  remark           varchar(500)    default null               comment '澶囨敞',
  primary key (dict_code)
) engine=innodb auto_increment=100 comment = '瀛楀吀鏁版嵁琛?;

insert into sys_dict_data values(1,  1,  '鐢?,       '0',       'sys_user_sex',        '',   '',        'Y', '0', 'admin', sysdate(), '', null, '鎬у埆鐢?);
insert into sys_dict_data values(2,  2,  '濂?,       '1',       'sys_user_sex',        '',   '',        'N', '0', 'admin', sysdate(), '', null, '鎬у埆濂?);
insert into sys_dict_data values(3,  3,  '鏈煡',     '2',       'sys_user_sex',        '',   '',        'N', '0', 'admin', sysdate(), '', null, '鎬у埆鏈煡');
insert into sys_dict_data values(4,  1,  '鏄剧ず',     '0',       'sys_show_hide',       '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '鏄剧ず鑿滃崟');
insert into sys_dict_data values(5,  2,  '闅愯棌',     '1',       'sys_show_hide',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '闅愯棌鑿滃崟');
insert into sys_dict_data values(6,  1,  '姝ｅ父',     '0',       'sys_normal_disable',  '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '姝ｅ父鐘舵€?);
insert into sys_dict_data values(7,  2,  '鍋滅敤',     '1',       'sys_normal_disable',  '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '鍋滅敤鐘舵€?);
insert into sys_dict_data values(8,  1,  '姝ｅ父',     '0',       'sys_job_status',      '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '姝ｅ父鐘舵€?);
insert into sys_dict_data values(9,  2,  '鏆傚仠',     '1',       'sys_job_status',      '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '鍋滅敤鐘舵€?);
insert into sys_dict_data values(10, 1,  '榛樿',     'DEFAULT', 'sys_job_group',       '',   '',        'Y', '0', 'admin', sysdate(), '', null, '榛樿鍒嗙粍');
insert into sys_dict_data values(11, 2,  '绯荤粺',     'SYSTEM',  'sys_job_group',       '',   '',        'N', '0', 'admin', sysdate(), '', null, '绯荤粺鍒嗙粍');
insert into sys_dict_data values(12, 1,  '鏄?,       'Y',       'sys_yes_no',          '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '绯荤粺榛樿鏄?);
insert into sys_dict_data values(13, 2,  '鍚?,       'N',       'sys_yes_no',          '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '绯荤粺榛樿鍚?);
insert into sys_dict_data values(14, 1,  '閫氱煡',     '1',       'sys_notice_type',     '',   'warning', 'Y', '0', 'admin', sysdate(), '', null, '閫氱煡');
insert into sys_dict_data values(15, 2,  '鍏憡',     '2',       'sys_notice_type',     '',   'success', 'N', '0', 'admin', sysdate(), '', null, '鍏憡');
insert into sys_dict_data values(16, 1,  '姝ｅ父',     '0',       'sys_notice_status',   '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '姝ｅ父鐘舵€?);
insert into sys_dict_data values(17, 2,  '鍏抽棴',     '1',       'sys_notice_status',   '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '鍏抽棴鐘舵€?);
insert into sys_dict_data values(18, 99, '鍏朵粬',     '0',       'sys_oper_type',       '',   'info',    'N', '0', 'admin', sysdate(), '', null, '鍏朵粬鎿嶄綔');
insert into sys_dict_data values(19, 1,  '鏂板',     '1',       'sys_oper_type',       '',   'info',    'N', '0', 'admin', sysdate(), '', null, '鏂板鎿嶄綔');
insert into sys_dict_data values(20, 2,  '淇敼',     '2',       'sys_oper_type',       '',   'info',    'N', '0', 'admin', sysdate(), '', null, '淇敼鎿嶄綔');
insert into sys_dict_data values(21, 3,  '鍒犻櫎',     '3',       'sys_oper_type',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '鍒犻櫎鎿嶄綔');
insert into sys_dict_data values(22, 4,  '鎺堟潈',     '4',       'sys_oper_type',       '',   'primary', 'N', '0', 'admin', sysdate(), '', null, '鎺堟潈鎿嶄綔');
insert into sys_dict_data values(23, 5,  '瀵煎嚭',     '5',       'sys_oper_type',       '',   'warning', 'N', '0', 'admin', sysdate(), '', null, '瀵煎嚭鎿嶄綔');
insert into sys_dict_data values(24, 6,  '瀵煎叆',     '6',       'sys_oper_type',       '',   'warning', 'N', '0', 'admin', sysdate(), '', null, '瀵煎叆鎿嶄綔');
insert into sys_dict_data values(25, 7,  '寮洪€€',     '7',       'sys_oper_type',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '寮洪€€鎿嶄綔');
insert into sys_dict_data values(26, 8,  '鐢熸垚浠ｇ爜', '8',       'sys_oper_type',       '',   'warning', 'N', '0', 'admin', sysdate(), '', null, '鐢熸垚鎿嶄綔');
insert into sys_dict_data values(27, 9,  '娓呯┖鏁版嵁', '9',       'sys_oper_type',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '娓呯┖鎿嶄綔');
insert into sys_dict_data values(28, 1,  '鎴愬姛',     '0',       'sys_common_status',   '',   'primary', 'N', '0', 'admin', sysdate(), '', null, '姝ｅ父鐘舵€?);
insert into sys_dict_data values(29, 2,  '澶辫触',     '1',       'sys_common_status',   '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '鍋滅敤鐘舵€?);


-- ----------------------------
-- 13銆佸弬鏁伴厤缃〃
-- ----------------------------
drop table if exists sys_config;
create table sys_config (
  config_id         int(5)          not null auto_increment    comment '鍙傛暟涓婚敭',
  config_name       varchar(100)    default ''                 comment '鍙傛暟鍚嶇О',
  config_key        varchar(100)    default ''                 comment '鍙傛暟閿悕',
  config_value      varchar(500)    default ''                 comment '鍙傛暟閿€?,
  config_type       char(1)         default 'N'                comment '绯荤粺鍐呯疆锛圷鏄?N鍚︼級',
  create_by         varchar(64)     default ''                 comment '鍒涘缓鑰?,
  create_time       datetime                                   comment '鍒涘缓鏃堕棿',
  update_by         varchar(64)     default ''                 comment '鏇存柊鑰?,
  update_time       datetime                                   comment '鏇存柊鏃堕棿',
  remark            varchar(500)    default null               comment '澶囨敞',
  primary key (config_id)
) engine=innodb auto_increment=100 comment = '鍙傛暟閰嶇疆琛?;

insert into sys_config values(1, '涓绘鏋堕〉-榛樿鐨偆鏍峰紡鍚嶇О',     'sys.index.skinName',            'skin-blue',     'Y', 'admin', sysdate(), '', null, '钃濊壊 skin-blue銆佺豢鑹?skin-green銆佺传鑹?skin-purple銆佺孩鑹?skin-red銆侀粍鑹?skin-yellow' );
insert into sys_config values(2, '鐢ㄦ埛绠＄悊-璐﹀彿鍒濆瀵嗙爜',         'sys.user.initPassword',         '123456',        'Y', 'admin', sysdate(), '', null, '鍒濆鍖栧瘑鐮?123456' );
insert into sys_config values(3, '涓绘鏋堕〉-渚ц竟鏍忎富棰?,           'sys.index.sideTheme',           'theme-dark',    'Y', 'admin', sysdate(), '', null, '娣辫壊涓婚theme-dark锛屾祬鑹蹭富棰榯heme-light' );
insert into sys_config values(4, '璐﹀彿鑷姪-楠岃瘉鐮佸紑鍏?,           'sys.account.captchaEnabled',    'true',          'Y', 'admin', sysdate(), '', null, '鏄惁寮€鍚獙璇佺爜鍔熻兘锛坱rue寮€鍚紝false鍏抽棴锛?);
insert into sys_config values(5, '璐﹀彿鑷姪-鏄惁寮€鍚敤鎴锋敞鍐屽姛鑳?, 'sys.account.registerUser',      'false',         'Y', 'admin', sysdate(), '', null, '鏄惁寮€鍚敞鍐岀敤鎴峰姛鑳斤紙true寮€鍚紝false鍏抽棴锛?);
insert into sys_config values(6, '鐢ㄦ埛鐧诲綍-榛戝悕鍗曞垪琛?,           'sys.login.blackIPList',         '',              'Y', 'admin', sysdate(), '', null, '璁剧疆鐧诲綍IP榛戝悕鍗曢檺鍒讹紝澶氫釜鍖归厤椤逛互;鍒嗛殧锛屾敮鎸佸尮閰嶏紙*閫氶厤銆佺綉娈碉級');


-- ----------------------------
-- 14銆佺郴缁熻闂褰?
-- ----------------------------
drop table if exists sys_logininfor;
create table sys_logininfor (
  info_id        bigint(20)     not null auto_increment   comment '璁块棶ID',
  user_name      varchar(50)    default ''                comment '鐢ㄦ埛璐﹀彿',
  ipaddr         varchar(128)   default ''                comment '鐧诲綍IP鍦板潃',
  login_location varchar(255)   default ''                comment '鐧诲綍鍦扮偣',
  browser        varchar(50)    default ''                comment '娴忚鍣ㄧ被鍨?,
  os             varchar(50)    default ''                comment '鎿嶄綔绯荤粺',
  status         char(1)        default '0'               comment '鐧诲綍鐘舵€侊紙0鎴愬姛 1澶辫触锛?,
  msg            varchar(255)   default ''                comment '鎻愮ず娑堟伅',
  login_time     datetime                                 comment '璁块棶鏃堕棿',
  primary key (info_id),
  key idx_sys_logininfor_s  (status),
  key idx_sys_logininfor_lt (login_time)
) engine=innodb auto_increment=100 comment = '绯荤粺璁块棶璁板綍';


-- ----------------------------
-- 15銆佸畾鏃朵换鍔¤皟搴﹁〃
-- ----------------------------
drop table if exists sys_job;
create table sys_job (
  job_id              bigint(20)    not null auto_increment    comment '浠诲姟ID',
  job_name            varchar(64)   default ''                 comment '浠诲姟鍚嶇О',
  job_group           varchar(64)   default 'DEFAULT'          comment '浠诲姟缁勫悕',
  invoke_target       varchar(500)  not null                   comment '璋冪敤鐩爣瀛楃涓?,
  cron_expression     varchar(255)  default ''                 comment 'cron鎵ц琛ㄨ揪寮?,
  misfire_policy      varchar(20)   default '3'                comment '璁″垝鎵ц閿欒绛栫暐锛?绔嬪嵆鎵ц 2鎵ц涓€娆?3鏀惧純鎵ц锛?,
  concurrent          char(1)       default '1'                comment '鏄惁骞跺彂鎵ц锛?鍏佽 1绂佹锛?,
  status              char(1)       default '0'                comment '鐘舵€侊紙0姝ｅ父 1鏆傚仠锛?,
  create_by           varchar(64)   default ''                 comment '鍒涘缓鑰?,
  create_time         datetime                                 comment '鍒涘缓鏃堕棿',
  update_by           varchar(64)   default ''                 comment '鏇存柊鑰?,
  update_time         datetime                                 comment '鏇存柊鏃堕棿',
  remark              varchar(500)  default ''                 comment '澶囨敞淇℃伅',
  primary key (job_id, job_name, job_group)
) engine=innodb auto_increment=100 comment = '瀹氭椂浠诲姟璋冨害琛?;

insert into sys_job values(1, '绯荤粺榛樿锛堟棤鍙傦級', 'DEFAULT', 'ryTask.ryNoParams',        '0/10 * * * * ?', '3', '1', '1', 'admin', sysdate(), '', null, '');
insert into sys_job values(2, '绯荤粺榛樿锛堟湁鍙傦級', 'DEFAULT', 'ryTask.ryParams(\'ry\')',  '0/15 * * * * ?', '3', '1', '1', 'admin', sysdate(), '', null, '');
insert into sys_job values(3, '绯荤粺榛樿锛堝鍙傦級', 'DEFAULT', 'ryTask.ryMultipleParams(\'ry\', true, 2000L, 316.50D, 100)',  '0/20 * * * * ?', '3', '1', '1', 'admin', sysdate(), '', null, '');


-- ----------------------------
-- 16銆佸畾鏃朵换鍔¤皟搴︽棩蹇楄〃
-- ----------------------------
drop table if exists sys_job_log;
create table sys_job_log (
  job_log_id          bigint(20)     not null auto_increment    comment '浠诲姟鏃ュ織ID',
  job_name            varchar(64)    not null                   comment '浠诲姟鍚嶇О',
  job_group           varchar(64)    not null                   comment '浠诲姟缁勫悕',
  invoke_target       varchar(500)   not null                   comment '璋冪敤鐩爣瀛楃涓?,
  job_message         varchar(500)                              comment '鏃ュ織淇℃伅',
  status              char(1)        default '0'                comment '鎵ц鐘舵€侊紙0姝ｅ父 1澶辫触锛?,
  exception_info      varchar(2000)  default ''                 comment '寮傚父淇℃伅',
  create_time         datetime                                  comment '鍒涘缓鏃堕棿',
  primary key (job_log_id)
) engine=innodb comment = '瀹氭椂浠诲姟璋冨害鏃ュ織琛?;


-- ----------------------------
-- 17銆侀€氱煡鍏憡琛?
-- ----------------------------
drop table if exists sys_notice;
create table sys_notice (
  notice_id         int(4)          not null auto_increment    comment '鍏憡ID',
  notice_title      varchar(50)     not null                   comment '鍏憡鏍囬',
  notice_type       char(1)         not null                   comment '鍏憡绫诲瀷锛?閫氱煡 2鍏憡锛?,
  notice_content    longblob        default null               comment '鍏憡鍐呭',
  status            char(1)         default '0'                comment '鍏憡鐘舵€侊紙0姝ｅ父 1鍏抽棴锛?,
  create_by         varchar(64)     default ''                 comment '鍒涘缓鑰?,
  create_time       datetime                                   comment '鍒涘缓鏃堕棿',
  update_by         varchar(64)     default ''                 comment '鏇存柊鑰?,
  update_time       datetime                                   comment '鏇存柊鏃堕棿',
  remark            varchar(255)    default null               comment '澶囨敞',
  primary key (notice_id)
) engine=innodb auto_increment=10 comment = '閫氱煡鍏憡琛?;

-- ----------------------------
-- 鍒濆鍖?鍏憡淇℃伅琛ㄦ暟鎹?
-- ----------------------------
insert into sys_notice values('1', '娓╅Θ鎻愰啋锛?018-07-01 鑻ヤ緷鏂扮増鏈彂甯冨暒', '2', '鏂扮増鏈唴瀹?, '0', 'admin', sysdate(), '', null, '绠＄悊鍛?);
insert into sys_notice values('2', '缁存姢閫氱煡锛?018-07-01 鑻ヤ緷绯荤粺鍑屾櫒缁存姢', '1', '缁存姢鍐呭',   '0', 'admin', sysdate(), '', null, '绠＄悊鍛?);


-- ----------------------------
-- 18銆佷唬鐮佺敓鎴愪笟鍔¤〃
-- ----------------------------
drop table if exists gen_table;
create table gen_table (
  table_id          bigint(20)      not null auto_increment    comment '缂栧彿',
  table_name        varchar(200)    default ''                 comment '琛ㄥ悕绉?,
  table_comment     varchar(500)    default ''                 comment '琛ㄦ弿杩?,
  sub_table_name    varchar(64)     default null               comment '鍏宠仈瀛愯〃鐨勮〃鍚?,
  sub_table_fk_name varchar(64)     default null               comment '瀛愯〃鍏宠仈鐨勫閿悕',
  class_name        varchar(100)    default ''                 comment '瀹炰綋绫诲悕绉?,
  tpl_category      varchar(200)    default 'crud'             comment '浣跨敤鐨勬ā鏉匡紙crud鍗曡〃鎿嶄綔 tree鏍戣〃鎿嶄綔锛?,
  tpl_web_type      varchar(30)     default ''                 comment '鍓嶇妯℃澘绫诲瀷锛坋lement-ui妯＄増 element-plus妯＄増锛?,
  package_name      varchar(100)                               comment '鐢熸垚鍖呰矾寰?,
  module_name       varchar(30)                                comment '鐢熸垚妯″潡鍚?,
  business_name     varchar(30)                                comment '鐢熸垚涓氬姟鍚?,
  function_name     varchar(50)                                comment '鐢熸垚鍔熻兘鍚?,
  function_author   varchar(50)                                comment '鐢熸垚鍔熻兘浣滆€?,
  gen_type          char(1)         default '0'                comment '鐢熸垚浠ｇ爜鏂瑰紡锛?zip鍘嬬缉鍖?1鑷畾涔夎矾寰勶級',
  gen_path          varchar(200)    default '/'                comment '鐢熸垚璺緞锛堜笉濉粯璁ら」鐩矾寰勶級',
  options           varchar(1000)                              comment '鍏跺畠鐢熸垚閫夐」',
  create_by         varchar(64)     default ''                 comment '鍒涘缓鑰?,
  create_time 	    datetime                                   comment '鍒涘缓鏃堕棿',
  update_by         varchar(64)     default ''                 comment '鏇存柊鑰?,
  update_time       datetime                                   comment '鏇存柊鏃堕棿',
  remark            varchar(500)    default null               comment '澶囨敞',
  primary key (table_id)
) engine=innodb auto_increment=1 comment = '浠ｇ爜鐢熸垚涓氬姟琛?;


-- ----------------------------
-- 19銆佷唬鐮佺敓鎴愪笟鍔¤〃瀛楁
-- ----------------------------
drop table if exists gen_table_column;
create table gen_table_column (
  column_id         bigint(20)      not null auto_increment    comment '缂栧彿',
  table_id          bigint(20)                                 comment '褰掑睘琛ㄧ紪鍙?,
  column_name       varchar(200)                               comment '鍒楀悕绉?,
  column_comment    varchar(500)                               comment '鍒楁弿杩?,
  column_type       varchar(100)                               comment '鍒楃被鍨?,
  java_type         varchar(500)                               comment 'JAVA绫诲瀷',
  java_field        varchar(200)                               comment 'JAVA瀛楁鍚?,
  is_pk             char(1)                                    comment '鏄惁涓婚敭锛?鏄級',
  is_increment      char(1)                                    comment '鏄惁鑷锛?鏄級',
  is_required       char(1)                                    comment '鏄惁蹇呭～锛?鏄級',
  is_insert         char(1)                                    comment '鏄惁涓烘彃鍏ュ瓧娈碉紙1鏄級',
  is_edit           char(1)                                    comment '鏄惁缂栬緫瀛楁锛?鏄級',
  is_list           char(1)                                    comment '鏄惁鍒楄〃瀛楁锛?鏄級',
  is_query          char(1)                                    comment '鏄惁鏌ヨ瀛楁锛?鏄級',
  query_type        varchar(200)    default 'EQ'               comment '鏌ヨ鏂瑰紡锛堢瓑浜庛€佷笉绛変簬銆佸ぇ浜庛€佸皬浜庛€佽寖鍥达級',
  html_type         varchar(200)                               comment '鏄剧ず绫诲瀷锛堟枃鏈銆佹枃鏈煙銆佷笅鎷夋銆佸閫夋銆佸崟閫夋銆佹棩鏈熸帶浠讹級',
  dict_type         varchar(200)    default ''                 comment '瀛楀吀绫诲瀷',
  sort              int                                        comment '鎺掑簭',
  create_by         varchar(64)     default ''                 comment '鍒涘缓鑰?,
  create_time 	    datetime                                   comment '鍒涘缓鏃堕棿',
  update_by         varchar(64)     default ''                 comment '鏇存柊鑰?,
  update_time       datetime                                   comment '鏇存柊鏃堕棿',
  primary key (column_id)
) engine=innodb auto_increment=1 comment = '浠ｇ爜鐢熸垚涓氬姟琛ㄥ瓧娈?;

-- ==============================
-- Remove code generator menu/perms (qkyd cleanup)
-- ==============================
DELETE FROM sys_role_menu WHERE menu_id IN (116,1055,1056,1057,1058,1059,1060);
DELETE FROM sys_menu WHERE menu_id IN (116,1055,1056,1057,1058,1059,1060);

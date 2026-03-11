import { createIconifyOfflineIcon } from '@vben-core/icons';

import keyboardEsc from '@iconify/icons-mdi/keyboard-esc';
import wechat from '@iconify/icons-mdi/wechat';
import github from '@iconify/icons-mdi/github';
import google from '@iconify/icons-mdi/google';
import qqchat from '@iconify/icons-mdi/qqchat';
import systemGroup from '@iconify/icons-eos-icons/system-group';
import profileLine from '@iconify/icons-mingcute/profile-line';
import dingdingFill from '@iconify/icons-ri/dingding-fill';

export * from '@vben-core/icons';

export const MdiKeyboardEsc = createIconifyOfflineIcon(
  'mdi:keyboard-esc',
  keyboardEsc,
);

export const MdiWechat = createIconifyOfflineIcon('mdi:wechat', wechat);

export const MdiGithub = createIconifyOfflineIcon('mdi:github', github);

export const MdiGoogle = createIconifyOfflineIcon('mdi:google', google);

export const MdiQqchat = createIconifyOfflineIcon('mdi:qqchat', qqchat);

export const EosSystem = createIconifyOfflineIcon(
  'eos-icons:system-group',
  systemGroup,
);

// 个人中心
export const ProfileIcon = createIconifyOfflineIcon(
  'mingcute:profile-line',
  profileLine,
);
export const RiDingding = createIconifyOfflineIcon(
  'ri:dingding-fill',
  dingdingFill,
);

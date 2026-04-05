<template>
  <div class="admin-page settings-page">
    <div class="admin-card">
      <div class="settings-header">
        <h2 class="admin-title"><i class="fas fa-star"></i> 系统设置</h2>
      </div>

      <template v-for="(group, groupKey) in groupedSettings" :key="groupKey">
        <div v-if="group.list.length" class="settings-section">
          <h3 class="section-title">
            <i :class="group.icon"></i>
            {{ group.title }}
          </h3>
          <div class="settings-table">
            <div class="settings-thead">
              <div class="th th-item">配置项</div>
              <div class="th th-value">当前值</div>
              <div class="th th-desc">说明</div>
              <div class="th th-action">操作</div>
            </div>
            <div
              v-for="row in group.list"
              :key="row.id"
              class="settings-row"
            >
              <div class="td td-item">
                <span class="item-name">{{ itemLabel(row.settingKey) }}</span>
              </div>
              <div class="td td-value">
                <span :class="['value-badge', valueBadgeClass(row)]">
                  <i v-if="isBoolean(row) && boolVal(row)" class="fas fa-check"></i>
                  <i v-else-if="isBoolean(row) && !boolVal(row)" class="fas fa-times"></i>
                  {{ valueDisplay(row) }}
                </span>
              </div>
              <div class="td td-desc">{{ row.description || '—' }}</div>
              <div class="td td-action">
                <button type="button" class="btn-edit" @click="edit(row)">
                  <i class="fas fa-pencil-alt"></i> 编辑
                </button>
              </div>
            </div>
          </div>
        </div>
      </template>

      <div class="settings-tip">
        <i class="fas fa-info-circle"></i>
        <span>共 {{ settings.length }} 项配置，点击编辑可修改。修改后会自动生效 (APPLIED闭环)。</span>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" title="编辑系统设置" width="560px" class="admin-dialog">
      <el-form :model="form" label-width="100px">
        <el-form-item label="配置项">
          <el-input :model-value="currentLabel" disabled />
        </el-form-item>
        <el-form-item label="当前设置">
          <el-select v-if="currentMeta?.kind === 'boolean'" v-model="form.settingValue" style="width: 100%">
            <el-option label="开启" value="true" />
            <el-option label="关闭" value="false" />
          </el-select>
          <el-input
            v-else
            v-model="form.settingValue"
            type="textarea"
            :rows="4"
            :placeholder="currentMeta?.placeholder || '请输入设置内容'"
          />
        </el-form-item>
        <el-form-item label="说明">
          <div class="dialog-help">{{ currentMeta?.desc || '修改后将立即生效。' }}</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button class="btn-primary" @click="save"><i class="fas fa-save"></i> 保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { listSettingsApi, updateSettingApi } from '@/api/settings'

const settings = ref([])
const dialogVisible = ref(false)
const form = reactive({ settingKey: '', settingValue: '' })

const meta = {
  'forum.allowRegister': { label: '允许新用户注册', section: 'register', kind: 'boolean', desc: '开启后，新用户可以自主注册账号。关闭后仅管理员可添加用户。' },
  'forum.postAutoPublish': { label: '帖子自动发布', section: 'register', kind: 'boolean', desc: '开启后，用户提交帖子可直接发布；关闭后将进入审核流程。' },
  'forum.emailVerify': { label: '注册邮箱', section: 'register', kind: 'boolean', desc: '开启后，新注册用户需要填写邮箱（不需要邮箱验证即可登录）。' },
  'forum.maxAttachments': { label: '每个帖子最大附件数量', section: 'attach', desc: '建议填写数字，例如：5。' },
  'forum.postMinLength': { label: '帖子最小字数限制', section: 'attach', desc: '建议填写数字，例如：10（不含标题）。' },
  'forum.imageMaxSizeKb': { label: '图片上传大小限制', section: 'attach', desc: '请填写图片大小上限（单位 KB），例如：2048。' },
  'forum.sensitiveWordFilter': { label: '敏感词过滤开关', section: 'security', kind: 'boolean', desc: '开启后，系统会自动检测帖子中的敏感词。' },
  'forum.sensitiveWords': { label: '敏感词词库', section: 'security', desc: '多个词用英文逗号分隔，例如：赌博,毒品,暴力。' }
}

const sections = {
  register: { title: '注册与发布', icon: 'fas fa-user-plus' },
  attach: { title: '附件与内容限制', icon: 'fas fa-paperclip' },
  security: { title: '安全与过滤', icon: 'fas fa-shield-alt' }
}

const groupedSettings = computed(() => {
  const otherMeta = { title: '其他', icon: 'fas fa-cog' }
  const groups = {
    register: { ...sections.register, list: [] },
    attach: { ...sections.attach, list: [] },
    security: { ...sections.security, list: [] },
    other: { ...otherMeta, list: [] }
  }
  settings.value.forEach((row) => {
    const m = meta[row.settingKey]
    const key = m?.section || 'other'
    groups[key].list.push(row)
  })
  return groups
})

function itemLabel(key) {
  return meta[key]?.label || key
}

const currentMeta = computed(() => meta[form.settingKey] || null)
const currentLabel = computed(() => itemLabel(form.settingKey))

function normalizeBoolValue(v) {
  const s = String(v ?? '').trim().toLowerCase()
  if (['true', '1', 'yes', 'on', '开启'].includes(s)) return 'true'
  if (['false', '0', 'no', 'off', '关闭'].includes(s)) return 'false'
  return ''
}

function isBoolean(row) {
  if (meta[row.settingKey]?.kind === 'boolean') return true
  const v = normalizeBoolValue(row.settingValue)
  return v === 'true' || v === 'false'
}

function boolVal(row) {
  return normalizeBoolValue(row.settingValue) === 'true'
}

function valueDisplay(row) {
  const v = (row.settingValue || '').trim()
  if (v === '') return '— 未设置 —'
  if (isBoolean(row)) return boolVal(row) ? 'true' : 'false'
  const m = meta[row.settingKey]
  if (m && row.settingKey === 'forum.imageMaxSizeKb') return v + ' KB'
  return v
}

function valueBadgeClass(row) {
  if (isBoolean(row)) return boolVal(row) ? 'value-badge--true' : 'value-badge--false'
  const v = (row.settingValue || '').trim()
  if (v === '') return 'value-badge--empty'
  return 'value-badge--plain'
}

const loadData = async () => {
  const data = await listSettingsApi()
  // 统一布尔配置的值，避免列表显示/编辑弹窗出现错乱
  settings.value = (data || []).map((row) => {
    const cloned = { ...row }
    if (meta[cloned.settingKey]?.kind === 'boolean') {
      const normalized = normalizeBoolValue(cloned.settingValue)
      if (normalized) cloned.settingValue = normalized
    }
    return cloned
  })
}

const edit = (row) => {
  form.settingKey = row.settingKey
  if (meta[row.settingKey]?.kind === 'boolean') {
    form.settingValue = normalizeBoolValue(row.settingValue) || 'false'
  } else {
    form.settingValue = row.settingValue
  }
  dialogVisible.value = true
}

const save = async () => {
  try {
    if (meta[form.settingKey]?.kind === 'boolean') {
      form.settingValue = normalizeBoolValue(form.settingValue) || 'false'
    }
    await updateSettingApi({
      settingKey: form.settingKey,
      settingValue: form.settingValue
    })
    ElMessage.success('配置已更新')
    dialogVisible.value = false
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || '更新失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.admin-page.settings-page { max-width: 1200px; padding: 0 0 24px; }
.admin-card {
  background: #fff;
  border-radius: 28px;
  border: 1px solid #ffedd5;
  box-shadow: 0 8px 24px rgba(255, 160, 5, 0.06);
  padding: 24px;
}

.settings-header {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 24px;
}
.admin-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #1f1f1f;
  display: flex;
  align-items: center;
  gap: 10px;
}
.admin-title i {
  color: #ffa005;
  font-size: 20px;
}
.admin-subtitle {
  font-size: 13px;
  color: #888;
}

.settings-section {
  margin-bottom: 28px;
}
.settings-section:last-of-type { margin-bottom: 0; }
.section-title {
  margin: 0 0 14px 0;
  font-size: 16px;
  font-weight: 700;
  color: #1f1f1f;
  display: flex;
  align-items: center;
  gap: 10px;
}
.section-title i {
  color: #ffa005;
  font-size: 15px;
}

.settings-table {
  border: 1px solid #f0f2f5;
  border-radius: 16px;
  overflow: hidden;
  background: #fafbfc;
}
.settings-thead {
  display: grid;
  grid-template-columns: 1fr 140px 1fr 100px;
  gap: 16px;
  padding: 14px 20px;
  background: #f0f2f5;
  font-weight: 700;
  font-size: 14px;
  color: #4e4e57;
}
.settings-row {
  display: grid;
  grid-template-columns: 1fr 140px 1fr 100px;
  gap: 16px;
  padding: 16px 20px;
  align-items: center;
  border-top: 1px solid #f0f2f5;
  background: #fff;
}
.settings-row:nth-child(even) { background: #fcfcfd; }

.td-item { min-width: 0; }
.item-name { display: block; font-weight: 600; color: #1f1f1f; margin-bottom: 4px; }

.value-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}
.value-badge--true { background: #e8f5e9; color: #2e7d32; }
.value-badge--false { background: #ffebee; color: #c62828; }
.value-badge--plain { background: #f0f2f5; color: #4e4e57; }
.value-badge--empty { background: #f0f2f5; color: #999; }

.td-desc { font-size: 13px; color: #5f6368; line-height: 1.5; }

.btn-edit {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 20px;
  border: none;
  background: #fffbf0;
  color: #ffa005;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: 0.15s;
}
.btn-edit:hover { background: #fff7e6; color: #e69404; }
.btn-edit i { font-size: 12px; }

.settings-tip {
  margin-top: 24px;
  padding: 14px 20px;
  background: #fffbf0;
  border: 1px solid #ffedd5;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: #8a6d3b;
}
.settings-tip i { color: #ffa005; font-size: 18px; flex-shrink: 0; }

.btn-primary { border-radius: 50px; background: #ffa005; border-color: #ffa005; }
.btn-primary:hover { background: #e69404; border-color: #e69404; }
.dialog-help {
  color: #666;
  line-height: 1.6;
  font-size: 13px;
}
</style>

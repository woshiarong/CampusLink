<template>
  <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
    <el-form-item label="版块" prop="boardId">
      <el-select v-model="form.boardId" placeholder="请选择版块" style="width: 100%">
        <el-option
          v-for="item in boards"
          :key="item.id"
          :label="item.name"
          :value="item.id"
        />
      </el-select>
    </el-form-item>

    <el-form-item label="标题" prop="title">
      <el-input v-model="form.title" maxlength="120" show-word-limit />
    </el-form-item>

    <el-form-item label="正文" prop="content">
      <el-input v-model="form.content" type="textarea" :rows="8" />
    </el-form-item>

    <el-form-item label="标签">
      <el-select v-model="form.tags" multiple allow-create filterable default-first-option style="width: 100%" />
    </el-form-item>

    <el-form-item label="附件">
      <el-upload
        class="upload-area"
        v-model:file-list="fileList"
        :http-request="onUpload"
        :on-remove="onRemove"
        :show-file-list="true"
        multiple
        list-type="text"
        :auto-upload="true"
      >
        <el-button type="primary" plain>选择文件</el-button>
        <template #tip>
          <div class="el-upload__tip">支持上传图片或文件，大小由后端限制。</div>
        </template>
      </el-upload>
    </el-form-item>

    <el-form-item v-if="!hideActions">
      <el-button type="primary" @click="onSave">{{ submitLabel }}</el-button>
      <el-button @click="onClear">清空</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { uploadFileApi } from '@/api/file'

const props = defineProps({
  boards: {
    type: Array,
    default: () => []
  },
  isEdit: { type: Boolean, default: false },
  hideActions: { type: Boolean, default: false }
})

const submitLabel = computed(() => (props.isEdit ? '更新' : '发布帖子'))

const emit = defineEmits(['save'])

const formRef = ref()
const form = reactive({
  boardId: null,
  title: '',
  content: '',
  tags: [],
  attachments: []
})

const rules = {
  boardId: [{ required: true, message: '请选择版块', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入正文', trigger: 'blur' }]
}

const fileList = ref([])
const uploadResultMap = ref(new Map())

const getFileUrl = (file) => {
  if (!file) return ''
  return file.url || file.response?.url || ''
}

const syncAttachmentsFromFileList = () => {
  const seen = new Set()
  form.attachments = (fileList.value || [])
    .map((f) => {
      const mapped = uploadResultMap.value.get(f?.uid)
      return {
        fileName: mapped?.fileName || f?.name,
        fileUrl: mapped?.fileUrl || getFileUrl(f)
      }
    })
    // 只保留上传成功（拿到 URL）的附件，避免把失败/临时文件算进数量
    .filter((f) => f.fileName && f.fileUrl)
    .filter((f) => {
      if (seen.has(f.fileUrl)) return false
      seen.add(f.fileUrl)
      return true
    })
}

const onUpload = async (option) => {
  try {
    const res = await uploadFileApi(option.file)
    // 不要直接修改原生 File 对象（name 是只读），通过 onSuccess 回传结果给 el-upload
    uploadResultMap.value.set(option.file?.uid, {
      fileName: res.fileName || option.file?.name,
      fileUrl: res.url
    })
    option.onSuccess(res)
    Promise.resolve().then(syncAttachmentsFromFileList)
  } catch (error) {
    ElMessage.error(error.message || '上传失败')
    option.onError(error)
  }
}

const onRemove = () => {
  const validUids = new Set((fileList.value || []).map((f) => f?.uid))
  for (const uid of uploadResultMap.value.keys()) {
    if (!validUids.has(uid)) uploadResultMap.value.delete(uid)
  }
  syncAttachmentsFromFileList()
}

const onSave = async () => {
  await formRef.value.validate()
  syncAttachmentsFromFileList()
  emit('save', JSON.parse(JSON.stringify(form)))
}

const onClear = async () => {
  try {
    await ElMessageBox.confirm('确定清空已填写内容？', '提示')
    formRef.value.resetFields()
    form.tags = []
    form.attachments = []
    fileList.value = []
    uploadResultMap.value.clear()
  } catch (_) {}
}

defineExpose({ onSave, onClear })
</script>

<style scoped>
.upload-area {
  width: 100%;
}
</style>

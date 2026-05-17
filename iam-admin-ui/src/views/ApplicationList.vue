<template>
  <div class="application-list">
    <div class="page-header">
      <h2>应用管理</h2>
      <el-button type="primary" @click="showCreateDialog">新增应用</el-button>
    </div>

    <el-table :data="applications" v-loading="loading" border>
      <el-table-column prop="appCode" label="应用编码" width="150" />
      <el-table-column prop="appName" label="应用名称" />
      <el-table-column prop="authProtocol" label="认证协议" width="150">
        <template #default="{ row }">
          <el-tag>{{ getProtocolLabel(row.authProtocol) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="callbackUrl" label="回调地址" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
          <el-button size="small" type="info" @click="showResourceConfig(row)">资源管理</el-button>
          <el-button size="small" type="warning" @click="showTenantAssignment(row)">租户分配</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Create/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑应用' : '新增应用'"
      width="700px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="应用编码" prop="appCode">
          <el-input v-model="form.appCode" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="应用名称" prop="appName">
          <el-input v-model="form.appName" />
        </el-form-item>
        <el-form-item label="认证协议" prop="authProtocol">
          <el-select v-model="form.authProtocol" @change="handleProtocolChange">
            <el-option label="OAuth2 + Authorization Code" value="OAUTH2_AUTHORIZATION_CODE" />
            <el-option label="OAuth2 + Password" value="OAUTH2_PASSWORD" />
            <el-option label="OIDC + Authorization Code" value="OIDC_AUTHORIZATION_CODE" />
            <el-option label="OIDC + Password" value="OIDC_PASSWORD" />
            <el-option label="CAS" value="CAS" />
            <el-option label="SAML" value="SAML" />
          </el-select>
        </el-form-item>
        <el-form-item label="回调地址" v-if="needsCallback">
          <el-input v-model="form.callbackUrl" placeholder="https://your-domain/callback" />
        </el-form-item>
        <el-form-item label="Client ID">
          <el-input v-model="form.clientId" />
        </el-form-item>
        <el-form-item label="Client Secret" v-if="needsSecret">
          <el-input v-model="form.clientSecret" type="password" show-password />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="禁用" value="INACTIVE" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- Resource Config Dialog -->
    <el-dialog v-model="resourceDialogVisible" title="资源管理" width="900px">
      <el-button type="primary" @click="showAddResource" style="margin-bottom: 15px">添加资源</el-button>
      <el-table :data="resources" border>
        <el-table-column prop="resourceCode" label="资源编码" />
        <el-table-column prop="resourceName" label="资源名称" />
        <el-table-column prop="resourceType" label="资源类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.resourceType === 'MENU' ? '' : (row.resourceType === 'BUTTON' ? 'warning' : 'danger')">
              {{ getTypeLabel(row.resourceType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路径" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="editResource(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteResource(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- Tenant Assignment Dialog -->
    <el-dialog v-model="tenantDialogVisible" title="租户分配" width="800px">
      <el-table :data="assignedTenants" border>
        <el-table-column prop="tenantCode" label="租户编码" />
        <el-table-column prop="tenantName" label="租户名称" />
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">
              {{ row.enabled ? '已启用' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="toggleAppStatus(row)" v-if="row.enabled">禁用</el-button>
            <el-button size="small" type="success" @click="toggleAppStatus(row)" v-else>启用</el-button>
            <el-button size="small" type="danger" @click="removeTenant(row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-button type="primary" @click="addTenantAssignment" style="margin-top: 15px">分配租户</el-button>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminApi } from '@/api/admin'

const applications = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const resourceDialogVisible = ref(false)
const tenantDialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const currentApp = ref(null)
const resources = ref([])
const assignedTenants = ref([])

const form = reactive({
  id: null,
  appCode: '',
  appName: '',
  authProtocol: 'OAUTH2_AUTHORIZATION_CODE',
  callbackUrl: '',
  clientId: '',
  clientSecret: '',
  description: '',
  status: 'ACTIVE'
})

const needsCallback = ref(true)
const needsSecret = ref(true)

const rules = {
  appCode: [{ required: true, message: '请输入应用编码', trigger: 'blur' }],
  appName: [{ required: true, message: '请输入应用名称', trigger: 'blur' }],
  authProtocol: [{ required: true, message: '请选择认证协议', trigger: 'change' }]
}

onMounted(() => {
  loadApplications()
})

const loadApplications = async () => {
  loading.value = true
  try {
    const response = await adminApi.getApplications()
    applications.value = response.content || response || []
  } catch (error) {
    ElMessage.error('加载应用列表失败')
  } finally {
    loading.value = false
  }
}

const showCreateDialog = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    appCode: '',
    appName: '',
    authProtocol: 'OAUTH2_AUTHORIZATION_CODE',
    callbackUrl: '',
    clientId: '',
    clientSecret: '',
    description: '',
    status: 'ACTIVE'
  })
  handleProtocolChange()
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  isEdit.value = true
  currentApp.value = row
  Object.assign(form, row)
  handleProtocolChange()
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      if (isEdit.value) {
        await adminApi.updateApplication(form.id, form)
        ElMessage.success('更新成功')
      } else {
        await adminApi.createApplication(form)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadApplications()
    } catch (error) {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除应用 "${row.appName}" 吗?`, '确认', { type: 'warning' })
    await adminApi.deleteApplication(row.id)
    ElMessage.success('删除成功')
    loadApplications()
  } catch (error) {
    // User cancelled or delete failed
  }
}

const handleProtocolChange = () => {
  const protocols = ['OAUTH2_AUTHORIZATION_CODE', 'OAUTH2_PASSWORD', 'OIDC_AUTHORIZATION_CODE', 'OIDC_PASSWORD']
  needsCallback.value = protocols.includes(form.authProtocol)
  needsSecret.value = !['CAS', 'SAML'].includes(form.authProtocol)
}

const getProtocolLabel = (protocol) => {
  const map = {
    OAUTH2_AUTHORIZATION_CODE: 'OAuth2授权码',
    OAUTH2_PASSWORD: 'OAuth2密码',
    OIDC_AUTHORIZATION_CODE: 'OIDC授权码',
    OIDC_PASSWORD: 'OIDC密码',
    CAS: 'CAS',
    SAML: 'SAML'
  }
  return map[protocol] || protocol
}

const getTypeLabel = (type) => {
  const map = { MENU: '菜单', BUTTON: '按钮', API: 'API' }
  return map[type] || type
}

const showResourceConfig = async (row) => {
  currentApp.value = row
  resourceDialogVisible.value = true
  resources.value = row.resources || []
}

const showAddResource = () => {
  ElMessage.info('添加资源功能开发中')
}

const editResource = () => {
  ElMessage.info('编辑资源功能开发中')
}

const deleteResource = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除此资源吗?', '确认', { type: 'warning' })
    await adminApi.deleteAppResource(row.id)
    ElMessage.success('删除成功')
    showResourceConfig(currentApp.value)
  } catch (error) {
    // User cancelled or delete failed
  }
}

const showTenantAssignment = async (row) => {
  currentApp.value = row
  tenantDialogVisible.value = true
  try {
    const response = await adminApi.getTenantApplications(row.tenantId || 0)
    assignedTenants.value = response || []
  } catch (error) {
    ElMessage.error('加载租户分配信息失败')
  }
}

const toggleAppStatus = async (row) => {
  try {
    if (row.enabled) {
      await adminApi.disableAppForTenant(row.id)
    } else {
      await adminApi.enableAppForTenant(row.id)
    }
    ElMessage.success('操作成功')
    showTenantAssignment(currentApp.value)
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const removeTenant = async (row) => {
  try {
    await ElMessageBox.confirm('确定移除此租户吗?', '确认', { type: 'warning' })
    ElMessage.success('移除成功')
    showTenantAssignment(currentApp.value)
  } catch (error) {
    // User cancelled
  }
}

const addTenantAssignment = () => {
  ElMessage.info('分配租户功能开发中')
}
</script>

<style scoped>
.application-list {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
}
</style>

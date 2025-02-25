// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.workspaceModel.storage.bridgeEntities.api

import com.intellij.workspaceModel.storage.*
import com.intellij.workspaceModel.storage.url.VirtualFileUrl
import org.jetbrains.deft.ObjBuilder
import org.jetbrains.deft.Type
import org.jetbrains.deft.annotations.Abstract
import org.jetbrains.deft.annotations.Child
import com.intellij.workspaceModel.storage.EntitySource
import com.intellij.workspaceModel.storage.GeneratedCodeApiVersion
import com.intellij.workspaceModel.storage.ModifiableWorkspaceEntity
import com.intellij.workspaceModel.storage.MutableEntityStorage



interface ArtifactEntity : WorkspaceEntityWithPersistentId {
    val name: String

    val artifactType: String
    val includeInProjectBuild: Boolean
    val outputUrl: VirtualFileUrl?

    @Child val rootElement: CompositePackagingElementEntity
    val customProperties: List<@Child ArtifactPropertiesEntity>
    @Child val artifactOutputPackagingElement: ArtifactOutputPackagingElementEntity?
    override val persistentId: ArtifactId
        get() = ArtifactId(name)

  //region generated code
  @GeneratedCodeApiVersion(1)
  interface Builder : ArtifactEntity, ModifiableWorkspaceEntity<ArtifactEntity>, ObjBuilder<ArtifactEntity> {
    override var name: String
    override var entitySource: EntitySource
    override var artifactType: String
    override var includeInProjectBuild: Boolean
    override var outputUrl: VirtualFileUrl?
    override var rootElement: CompositePackagingElementEntity
    override var customProperties: List<ArtifactPropertiesEntity>
    override var artifactOutputPackagingElement: ArtifactOutputPackagingElementEntity?
  }

  companion object : Type<ArtifactEntity, Builder>() {
    operator fun invoke(name: String,
                        artifactType: String,
                        includeInProjectBuild: Boolean,
                        entitySource: EntitySource,
                        init: (Builder.() -> Unit)? = null): ArtifactEntity {
      val builder = builder()
      builder.name = name
      builder.artifactType = artifactType
      builder.includeInProjectBuild = includeInProjectBuild
      builder.entitySource = entitySource
      init?.invoke(builder)
      return builder
    }
  }
  //endregion

}

//region generated code
fun MutableEntityStorage.modifyEntity(entity: ArtifactEntity, modification: ArtifactEntity.Builder.() -> Unit) = modifyEntity(
  ArtifactEntity.Builder::class.java, entity, modification)

var ArtifactEntity.Builder.artifactExternalSystemIdEntity: @Child ArtifactExternalSystemIdEntity?
  by WorkspaceEntity.extension()
//endregion

interface ArtifactPropertiesEntity : WorkspaceEntity {
    val artifact: ArtifactEntity

    val providerType: String
    val propertiesXmlTag: String?

  //region generated code
  @GeneratedCodeApiVersion(1)
  interface Builder : ArtifactPropertiesEntity, ModifiableWorkspaceEntity<ArtifactPropertiesEntity>, ObjBuilder<ArtifactPropertiesEntity> {
    override var artifact: ArtifactEntity
    override var entitySource: EntitySource
    override var providerType: String
    override var propertiesXmlTag: String?
  }

  companion object : Type<ArtifactPropertiesEntity, Builder>() {
    operator fun invoke(providerType: String, entitySource: EntitySource, init: (Builder.() -> Unit)? = null): ArtifactPropertiesEntity {
      val builder = builder()
      builder.providerType = providerType
      builder.entitySource = entitySource
      init?.invoke(builder)
      return builder
    }
  }
  //endregion

}

//region generated code
fun MutableEntityStorage.modifyEntity(entity: ArtifactPropertiesEntity,
                                      modification: ArtifactPropertiesEntity.Builder.() -> Unit) = modifyEntity(
  ArtifactPropertiesEntity.Builder::class.java, entity, modification)
//endregion

@Abstract interface PackagingElementEntity : WorkspaceEntity {
    val parentEntity: CompositePackagingElementEntity?

  //region generated code
  @GeneratedCodeApiVersion(1)
  interface Builder<T : PackagingElementEntity> : PackagingElementEntity, ModifiableWorkspaceEntity<T>, ObjBuilder<T> {
    override var parentEntity: CompositePackagingElementEntity?
    override var entitySource: EntitySource
  }

  companion object : Type<PackagingElementEntity, Builder<PackagingElementEntity>>() {
    operator fun invoke(entitySource: EntitySource, init: (Builder<PackagingElementEntity>.() -> Unit)? = null): PackagingElementEntity {
      val builder = builder()
      builder.entitySource = entitySource
      init?.invoke(builder)
      return builder
    }
  }
  //endregion

}

@Abstract interface CompositePackagingElementEntity : PackagingElementEntity {
    val artifact: ArtifactEntity?

    val children: List<@Child PackagingElementEntity>

  //region generated code
  @GeneratedCodeApiVersion(1)
  interface Builder<T : CompositePackagingElementEntity> : CompositePackagingElementEntity, PackagingElementEntity.Builder<T>, ModifiableWorkspaceEntity<T>, ObjBuilder<T> {
    override var parentEntity: CompositePackagingElementEntity?
    override var artifact: ArtifactEntity?
    override var entitySource: EntitySource
    override var children: List<PackagingElementEntity>
  }

  companion object : Type<CompositePackagingElementEntity, Builder<CompositePackagingElementEntity>>(PackagingElementEntity) {
    operator fun invoke(entitySource: EntitySource,
                        init: (Builder<CompositePackagingElementEntity>.() -> Unit)? = null): CompositePackagingElementEntity {
      val builder = builder()
      builder.entitySource = entitySource
      init?.invoke(builder)
      return builder
    }
  }
  //endregion

}

interface DirectoryPackagingElementEntity: CompositePackagingElementEntity {
    val directoryName: String

  //region generated code
  @GeneratedCodeApiVersion(1)
  interface Builder : DirectoryPackagingElementEntity, CompositePackagingElementEntity.Builder<DirectoryPackagingElementEntity>, ModifiableWorkspaceEntity<DirectoryPackagingElementEntity>, ObjBuilder<DirectoryPackagingElementEntity> {
    override var parentEntity: CompositePackagingElementEntity?
    override var artifact: ArtifactEntity?
    override var children: List<PackagingElementEntity>
    override var directoryName: String
    override var entitySource: EntitySource
  }

  companion object : Type<DirectoryPackagingElementEntity, Builder>(CompositePackagingElementEntity) {
    operator fun invoke(directoryName: String,
                        entitySource: EntitySource,
                        init: (Builder.() -> Unit)? = null): DirectoryPackagingElementEntity {
      val builder = builder()
      builder.directoryName = directoryName
      builder.entitySource = entitySource
      init?.invoke(builder)
      return builder
    }
  }
  //endregion

}

//region generated code
fun MutableEntityStorage.modifyEntity(entity: DirectoryPackagingElementEntity,
                                      modification: DirectoryPackagingElementEntity.Builder.() -> Unit) = modifyEntity(
  DirectoryPackagingElementEntity.Builder::class.java, entity, modification)
//endregion

interface ArchivePackagingElementEntity: CompositePackagingElementEntity {
    val fileName: String

  //region generated code
  @GeneratedCodeApiVersion(1)
  interface Builder : ArchivePackagingElementEntity, CompositePackagingElementEntity.Builder<ArchivePackagingElementEntity>, ModifiableWorkspaceEntity<ArchivePackagingElementEntity>, ObjBuilder<ArchivePackagingElementEntity> {
    override var parentEntity: CompositePackagingElementEntity?
    override var artifact: ArtifactEntity?
    override var children: List<PackagingElementEntity>
    override var fileName: String
    override var entitySource: EntitySource
  }

  companion object : Type<ArchivePackagingElementEntity, Builder>(CompositePackagingElementEntity) {
    operator fun invoke(fileName: String, entitySource: EntitySource, init: (Builder.() -> Unit)? = null): ArchivePackagingElementEntity {
      val builder = builder()
      builder.fileName = fileName
      builder.entitySource = entitySource
      init?.invoke(builder)
      return builder
    }
  }
  //endregion

}

//region generated code
fun MutableEntityStorage.modifyEntity(entity: ArchivePackagingElementEntity,
                                      modification: ArchivePackagingElementEntity.Builder.() -> Unit) = modifyEntity(
  ArchivePackagingElementEntity.Builder::class.java, entity, modification)
//endregion

interface ArtifactRootElementEntity: CompositePackagingElementEntity {
  //region generated code
  @GeneratedCodeApiVersion(1)
  interface Builder : ArtifactRootElementEntity, CompositePackagingElementEntity.Builder<ArtifactRootElementEntity>, ModifiableWorkspaceEntity<ArtifactRootElementEntity>, ObjBuilder<ArtifactRootElementEntity> {
    override var parentEntity: CompositePackagingElementEntity?
    override var artifact: ArtifactEntity?
    override var entitySource: EntitySource
    override var children: List<PackagingElementEntity>
  }

  companion object : Type<ArtifactRootElementEntity, Builder>(CompositePackagingElementEntity) {
    operator fun invoke(entitySource: EntitySource, init: (Builder.() -> Unit)? = null): ArtifactRootElementEntity {
      val builder = builder()
      builder.entitySource = entitySource
      init?.invoke(builder)
      return builder
    }
  }
  //endregion

}

//region generated code
fun MutableEntityStorage.modifyEntity(entity: ArtifactRootElementEntity,
                                      modification: ArtifactRootElementEntity.Builder.() -> Unit) = modifyEntity(
  ArtifactRootElementEntity.Builder::class.java, entity, modification)
//endregion

interface ArtifactOutputPackagingElementEntity: PackagingElementEntity {
    val artifact: ArtifactId?

  //region generated code
  @GeneratedCodeApiVersion(1)
  interface Builder : ArtifactOutputPackagingElementEntity, PackagingElementEntity.Builder<ArtifactOutputPackagingElementEntity>, ModifiableWorkspaceEntity<ArtifactOutputPackagingElementEntity>, ObjBuilder<ArtifactOutputPackagingElementEntity> {
    override var parentEntity: CompositePackagingElementEntity?
    override var artifact: ArtifactId?
    override var entitySource: EntitySource
  }

  companion object : Type<ArtifactOutputPackagingElementEntity, Builder>(PackagingElementEntity) {
    operator fun invoke(entitySource: EntitySource, init: (Builder.() -> Unit)? = null): ArtifactOutputPackagingElementEntity {
      val builder = builder()
      builder.entitySource = entitySource
      init?.invoke(builder)
      return builder
    }
  }
  //endregion

}

//region generated code
fun MutableEntityStorage.modifyEntity(entity: ArtifactOutputPackagingElementEntity,
                                      modification: ArtifactOutputPackagingElementEntity.Builder.() -> Unit) = modifyEntity(
  ArtifactOutputPackagingElementEntity.Builder::class.java, entity, modification)

var ArtifactOutputPackagingElementEntity.Builder.artifactEntity: ArtifactEntity
  by WorkspaceEntity.extension()
//endregion

val ArtifactOutputPackagingElementEntity.artifactEntity: ArtifactEntity
    by WorkspaceEntity.extension()

interface ModuleOutputPackagingElementEntity : PackagingElementEntity {
    val module: ModuleId?

  //region generated code
  @GeneratedCodeApiVersion(1)
  interface Builder : ModuleOutputPackagingElementEntity, PackagingElementEntity.Builder<ModuleOutputPackagingElementEntity>, ModifiableWorkspaceEntity<ModuleOutputPackagingElementEntity>, ObjBuilder<ModuleOutputPackagingElementEntity> {
    override var parentEntity: CompositePackagingElementEntity?
    override var module: ModuleId?
    override var entitySource: EntitySource
  }

  companion object : Type<ModuleOutputPackagingElementEntity, Builder>(PackagingElementEntity) {
    operator fun invoke(entitySource: EntitySource, init: (Builder.() -> Unit)? = null): ModuleOutputPackagingElementEntity {
      val builder = builder()
      builder.entitySource = entitySource
      init?.invoke(builder)
      return builder
    }
  }
  //endregion

}

//region generated code
fun MutableEntityStorage.modifyEntity(entity: ModuleOutputPackagingElementEntity,
                                      modification: ModuleOutputPackagingElementEntity.Builder.() -> Unit) = modifyEntity(
  ModuleOutputPackagingElementEntity.Builder::class.java, entity, modification)
//endregion

interface LibraryFilesPackagingElementEntity : PackagingElementEntity {
    val library: LibraryId?

  //region generated code
  @GeneratedCodeApiVersion(1)
  interface Builder : LibraryFilesPackagingElementEntity, PackagingElementEntity.Builder<LibraryFilesPackagingElementEntity>, ModifiableWorkspaceEntity<LibraryFilesPackagingElementEntity>, ObjBuilder<LibraryFilesPackagingElementEntity> {
    override var parentEntity: CompositePackagingElementEntity?
    override var library: LibraryId?
    override var entitySource: EntitySource
  }

  companion object : Type<LibraryFilesPackagingElementEntity, Builder>(PackagingElementEntity) {
    operator fun invoke(entitySource: EntitySource, init: (Builder.() -> Unit)? = null): LibraryFilesPackagingElementEntity {
      val builder = builder()
      builder.entitySource = entitySource
      init?.invoke(builder)
      return builder
    }
  }
  //endregion

}

//region generated code
fun MutableEntityStorage.modifyEntity(entity: LibraryFilesPackagingElementEntity,
                                      modification: LibraryFilesPackagingElementEntity.Builder.() -> Unit) = modifyEntity(
  LibraryFilesPackagingElementEntity.Builder::class.java, entity, modification)

var LibraryFilesPackagingElementEntity.Builder.libraryEntity: LibraryEntity
  by WorkspaceEntity.extension()
//endregion

val LibraryFilesPackagingElementEntity.libraryEntity: LibraryEntity
    by WorkspaceEntity.extension()

interface ModuleSourcePackagingElementEntity : PackagingElementEntity {
    val module: ModuleId?

  //region generated code
  @GeneratedCodeApiVersion(1)
  interface Builder : ModuleSourcePackagingElementEntity, PackagingElementEntity.Builder<ModuleSourcePackagingElementEntity>, ModifiableWorkspaceEntity<ModuleSourcePackagingElementEntity>, ObjBuilder<ModuleSourcePackagingElementEntity> {
    override var parentEntity: CompositePackagingElementEntity?
    override var module: ModuleId?
    override var entitySource: EntitySource
  }

  companion object : Type<ModuleSourcePackagingElementEntity, Builder>(PackagingElementEntity) {
    operator fun invoke(entitySource: EntitySource, init: (Builder.() -> Unit)? = null): ModuleSourcePackagingElementEntity {
      val builder = builder()
      builder.entitySource = entitySource
      init?.invoke(builder)
      return builder
    }
  }
  //endregion

}

//region generated code
fun MutableEntityStorage.modifyEntity(entity: ModuleSourcePackagingElementEntity,
                                      modification: ModuleSourcePackagingElementEntity.Builder.() -> Unit) = modifyEntity(
  ModuleSourcePackagingElementEntity.Builder::class.java, entity, modification)
//endregion

interface ModuleTestOutputPackagingElementEntity : PackagingElementEntity {
    val module: ModuleId?

  //region generated code
  @GeneratedCodeApiVersion(1)
  interface Builder : ModuleTestOutputPackagingElementEntity, PackagingElementEntity.Builder<ModuleTestOutputPackagingElementEntity>, ModifiableWorkspaceEntity<ModuleTestOutputPackagingElementEntity>, ObjBuilder<ModuleTestOutputPackagingElementEntity> {
    override var parentEntity: CompositePackagingElementEntity?
    override var module: ModuleId?
    override var entitySource: EntitySource
  }

  companion object : Type<ModuleTestOutputPackagingElementEntity, Builder>(PackagingElementEntity) {
    operator fun invoke(entitySource: EntitySource, init: (Builder.() -> Unit)? = null): ModuleTestOutputPackagingElementEntity {
      val builder = builder()
      builder.entitySource = entitySource
      init?.invoke(builder)
      return builder
    }
  }
  //endregion

}

//region generated code
fun MutableEntityStorage.modifyEntity(entity: ModuleTestOutputPackagingElementEntity,
                                      modification: ModuleTestOutputPackagingElementEntity.Builder.() -> Unit) = modifyEntity(
  ModuleTestOutputPackagingElementEntity.Builder::class.java, entity, modification)
//endregion

@Abstract interface FileOrDirectoryPackagingElementEntity : PackagingElementEntity {
    val filePath: VirtualFileUrl

  //region generated code
  @GeneratedCodeApiVersion(1)
  interface Builder<T : FileOrDirectoryPackagingElementEntity> : FileOrDirectoryPackagingElementEntity, PackagingElementEntity.Builder<T>, ModifiableWorkspaceEntity<T>, ObjBuilder<T> {
    override var parentEntity: CompositePackagingElementEntity?
    override var filePath: VirtualFileUrl
    override var entitySource: EntitySource
  }

  companion object : Type<FileOrDirectoryPackagingElementEntity, Builder<FileOrDirectoryPackagingElementEntity>>(PackagingElementEntity) {
    operator fun invoke(filePath: VirtualFileUrl,
                        entitySource: EntitySource,
                        init: (Builder<FileOrDirectoryPackagingElementEntity>.() -> Unit)? = null): FileOrDirectoryPackagingElementEntity {
      val builder = builder()
      builder.filePath = filePath
      builder.entitySource = entitySource
      init?.invoke(builder)
      return builder
    }
  }
  //endregion

}

interface DirectoryCopyPackagingElementEntity : FileOrDirectoryPackagingElementEntity {
  //region generated code
  @GeneratedCodeApiVersion(1)
  interface Builder : DirectoryCopyPackagingElementEntity, FileOrDirectoryPackagingElementEntity.Builder<DirectoryCopyPackagingElementEntity>, ModifiableWorkspaceEntity<DirectoryCopyPackagingElementEntity>, ObjBuilder<DirectoryCopyPackagingElementEntity> {
    override var parentEntity: CompositePackagingElementEntity?
    override var filePath: VirtualFileUrl
    override var entitySource: EntitySource
  }

  companion object : Type<DirectoryCopyPackagingElementEntity, Builder>(FileOrDirectoryPackagingElementEntity) {
    operator fun invoke(filePath: VirtualFileUrl,
                        entitySource: EntitySource,
                        init: (Builder.() -> Unit)? = null): DirectoryCopyPackagingElementEntity {
      val builder = builder()
      builder.filePath = filePath
      builder.entitySource = entitySource
      init?.invoke(builder)
      return builder
    }
  }
  //endregion

}

//region generated code
fun MutableEntityStorage.modifyEntity(entity: DirectoryCopyPackagingElementEntity,
                                      modification: DirectoryCopyPackagingElementEntity.Builder.() -> Unit) = modifyEntity(
  DirectoryCopyPackagingElementEntity.Builder::class.java, entity, modification)
//endregion

interface ExtractedDirectoryPackagingElementEntity: FileOrDirectoryPackagingElementEntity {
    val pathInArchive: String

  //region generated code
  @GeneratedCodeApiVersion(1)
  interface Builder : ExtractedDirectoryPackagingElementEntity, FileOrDirectoryPackagingElementEntity.Builder<ExtractedDirectoryPackagingElementEntity>, ModifiableWorkspaceEntity<ExtractedDirectoryPackagingElementEntity>, ObjBuilder<ExtractedDirectoryPackagingElementEntity> {
    override var parentEntity: CompositePackagingElementEntity?
    override var filePath: VirtualFileUrl
    override var pathInArchive: String
    override var entitySource: EntitySource
  }

  companion object : Type<ExtractedDirectoryPackagingElementEntity, Builder>(FileOrDirectoryPackagingElementEntity) {
    operator fun invoke(filePath: VirtualFileUrl,
                        pathInArchive: String,
                        entitySource: EntitySource,
                        init: (Builder.() -> Unit)? = null): ExtractedDirectoryPackagingElementEntity {
      val builder = builder()
      builder.filePath = filePath
      builder.pathInArchive = pathInArchive
      builder.entitySource = entitySource
      init?.invoke(builder)
      return builder
    }
  }
  //endregion

}

//region generated code
fun MutableEntityStorage.modifyEntity(entity: ExtractedDirectoryPackagingElementEntity,
                                      modification: ExtractedDirectoryPackagingElementEntity.Builder.() -> Unit) = modifyEntity(
  ExtractedDirectoryPackagingElementEntity.Builder::class.java, entity, modification)
//endregion

interface FileCopyPackagingElementEntity : FileOrDirectoryPackagingElementEntity {
    val renamedOutputFileName: String?

  //region generated code
  @GeneratedCodeApiVersion(1)
  interface Builder : FileCopyPackagingElementEntity, FileOrDirectoryPackagingElementEntity.Builder<FileCopyPackagingElementEntity>, ModifiableWorkspaceEntity<FileCopyPackagingElementEntity>, ObjBuilder<FileCopyPackagingElementEntity> {
    override var parentEntity: CompositePackagingElementEntity?
    override var filePath: VirtualFileUrl
    override var renamedOutputFileName: String?
    override var entitySource: EntitySource
  }

  companion object : Type<FileCopyPackagingElementEntity, Builder>(FileOrDirectoryPackagingElementEntity) {
    operator fun invoke(filePath: VirtualFileUrl,
                        entitySource: EntitySource,
                        init: (Builder.() -> Unit)? = null): FileCopyPackagingElementEntity {
      val builder = builder()
      builder.filePath = filePath
      builder.entitySource = entitySource
      init?.invoke(builder)
      return builder
    }
  }
  //endregion

}

//region generated code
fun MutableEntityStorage.modifyEntity(entity: FileCopyPackagingElementEntity,
                                      modification: FileCopyPackagingElementEntity.Builder.() -> Unit) = modifyEntity(
  FileCopyPackagingElementEntity.Builder::class.java, entity, modification)
//endregion

interface CustomPackagingElementEntity : CompositePackagingElementEntity {
    val typeId: String
    val propertiesXmlTag: String

  //region generated code
  @GeneratedCodeApiVersion(1)
  interface Builder : CustomPackagingElementEntity, CompositePackagingElementEntity.Builder<CustomPackagingElementEntity>, ModifiableWorkspaceEntity<CustomPackagingElementEntity>, ObjBuilder<CustomPackagingElementEntity> {
    override var parentEntity: CompositePackagingElementEntity?
    override var artifact: ArtifactEntity?
    override var children: List<PackagingElementEntity>
    override var typeId: String
    override var entitySource: EntitySource
    override var propertiesXmlTag: String
  }

  companion object : Type<CustomPackagingElementEntity, Builder>(CompositePackagingElementEntity) {
    operator fun invoke(typeId: String,
                        propertiesXmlTag: String,
                        entitySource: EntitySource,
                        init: (Builder.() -> Unit)? = null): CustomPackagingElementEntity {
      val builder = builder()
      builder.typeId = typeId
      builder.propertiesXmlTag = propertiesXmlTag
      builder.entitySource = entitySource
      init?.invoke(builder)
      return builder
    }
  }
  //endregion

}

//region generated code
fun MutableEntityStorage.modifyEntity(entity: CustomPackagingElementEntity,
                                      modification: CustomPackagingElementEntity.Builder.() -> Unit) = modifyEntity(
  CustomPackagingElementEntity.Builder::class.java, entity, modification)
//endregion

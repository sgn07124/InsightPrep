package com.project.InsightPrep.domain.post.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.project.InsightPrep.domain.member.entity.Member;
import com.project.InsightPrep.domain.post.dto.CommentRequest.CreateDto;
import com.project.InsightPrep.domain.post.dto.CommentRequest.UpdateDto;
import com.project.InsightPrep.domain.post.dto.CommentResponse.CommentListItem;
import com.project.InsightPrep.domain.post.dto.CommentResponse.CommentRes;
import com.project.InsightPrep.domain.post.dto.CommentResponse.CommentRow;
import com.project.InsightPrep.domain.post.entity.Comment;
import com.project.InsightPrep.domain.post.entity.SharedPost;
import com.project.InsightPrep.domain.post.exception.PostErrorCode;
import com.project.InsightPrep.domain.post.exception.PostException;
import com.project.InsightPrep.domain.post.mapper.CommentMapper;
import com.project.InsightPrep.domain.post.mapper.SharedPostMapper;
import com.project.InsightPrep.domain.post.reqository.CommentRepository;
import com.project.InsightPrep.domain.post.reqository.SharedPostRepository;
import com.project.InsightPrep.domain.question.dto.response.PageResponse;
import com.project.InsightPrep.global.auth.util.SecurityUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    SecurityUtil securityUtil;

    @Mock
    SharedPostMapper sharedPostMapper;

    @Mock
    SharedPostRepository sharedPostRepository;

    @Mock
    CommentMapper commentMapper;

    @Mock
    CommentRepository commentRepository;

    @InjectMocks
    CommentServiceImpl commentService;

    private static SharedPost stubPost(long id) {
        return SharedPost.builder().id(id).build();
    }

    private static CommentListItem item(long cid, long authorId, String nickname, String content, LocalDateTime ts) {
        return CommentListItem.builder()
                .commentId(cid)
                .authorId(authorId)
                .authorNickname(nickname)
                .content(content)
                .createdAt(ts)
                // service에서 mine 세팅하므로 여기선 넣지 않음
                .build();
    }

    // ===== createComment =====
    @Nested
    @DisplayName("createComment")
    class CreateComment {

        @Test
        @DisplayName("성공 - 게시글 존재 + 본인 인증 OK")
        void create_success() {
            long postId = 10L;
            Member me = Member.builder().id(1L).nickname("tester").build();
            SharedPost post = SharedPost.builder().id(postId).build();

            when(securityUtil.getAuthenticatedMember()).thenReturn(me);
            when(sharedPostRepository.findById(postId)).thenReturn(Optional.of(post));
            // insert 시 selectKey로 id가 세팅되는 것을 흉내
            doAnswer(inv -> {
                Comment c = inv.getArgument(0);
                try {
                    var idField = Comment.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(c, 777L);
                } catch (Exception ignored) {}
                return null;
            }).when(commentRepository).save(any(Comment.class));

            CreateDto req = new CreateDto("첫 댓글");

            CommentRes res = commentService.createComment(postId, req);

            assertThat(res.getCommentId()).isEqualTo(777L);
            assertThat(res.getContent()).isEqualTo("첫 댓글");
            assertThat(res.getAuthorId()).isEqualTo(1L);
            assertThat(res.getAuthorNickname()).isEqualTo("tester");
            assertThat(res.getPostId()).isEqualTo(postId);

            verify(securityUtil).getAuthenticatedMember();
            verify(sharedPostRepository).findById(postId);
            verify(commentRepository).save(any(Comment.class));
        }

        @Test
        @DisplayName("실패 - 게시글 없음 → POST_NOT_FOUND")
        void create_post_not_found() {
            long postId = 99L;
            when(sharedPostRepository.findById(postId)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    commentService.createComment(postId, new CreateDto("x"))
            ).isInstanceOf(PostException.class)
                    .hasMessageContaining(PostErrorCode.POST_NOT_FOUND.getMessage());

            verify(sharedPostRepository).findById(postId);
            verifyNoInteractions(commentMapper);
        }
    }

    // ===== updateComment =====
    @Nested
    @DisplayName("updateComment")
    class UpdateComment {

        @Test
        @DisplayName("성공 - 본인 댓글 & 같은 postId")
        void update_success() {
            // given
            long postId = 10L;
            long commentId = 200L;
            long me = 1L;

            Member member = Member.builder().id(me).nickname("tester").build();
            SharedPost post = SharedPost.builder().id(postId).build();
            Comment comment = Comment.builder()
                    .id(commentId)
                    .content("old")
                    .member(member)
                    .sharedPost(post)
                    .build();

            when(sharedPostRepository.findById(postId)).thenReturn(Optional.of(post));
            when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
            when(securityUtil.getLoginMemberId()).thenReturn(me);

            // when
            commentService.updateComment(postId, commentId, new UpdateDto("new content"));

            // then
            verify(sharedPostRepository).findById(postId);
            verify(commentRepository).findById(commentId);
            verify(securityUtil).getLoginMemberId();

            assertThat(comment.getContent()).isEqualTo("new content");
        }

        @Test
        @DisplayName("실패 - 게시글 없음 → POST_NOT_FOUND")
        void update_post_not_found() {
            long postId = 10L;
            long commentId = 200L;
            when(sharedPostRepository.findById(postId)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    commentService.updateComment(postId, commentId, new UpdateDto("x"))
            ).isInstanceOf(PostException.class)
                    .hasMessageContaining(PostErrorCode.POST_NOT_FOUND.getMessage());

            verify(sharedPostRepository).findById(postId);
            verifyNoMoreInteractions(sharedPostMapper);
            verifyNoInteractions(commentMapper, securityUtil);
        }

        @Test
        @DisplayName("실패 - 댓글 없음 → COMMENT_NOT_FOUND")
        void update_comment_not_found() {
            long postId = 10L;
            long commentId = 200L;

            when(sharedPostRepository.findById(postId)).thenReturn(Optional.of(SharedPost.builder().id(postId).build()));
            when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    commentService.updateComment(postId, commentId, new UpdateDto("x"))
            ).isInstanceOf(PostException.class)
                    .hasMessageContaining(PostErrorCode.COMMENT_NOT_FOUND.getMessage());

            verify(sharedPostRepository).findById(postId);
            verify(commentRepository).findById(commentId);
            verifyNoInteractions(securityUtil);
        }

        @Test
        @DisplayName("실패 - 다른 게시글의 댓글 → COMMENT_NOT_FOUND")
        void update_wrong_postId() {
            long postId = 10L;
            long commentId = 200L;

            SharedPost requestedPost = SharedPost.builder().id(postId).build();
            SharedPost otherPost = SharedPost.builder().id(999L).build();

            Member member = Member.builder().id(1L).build();
            Comment comment = Comment.builder()
                    .id(commentId)
                    .content("x")
                    .member(member)
                    .sharedPost(otherPost)
                    .build();

            when(sharedPostRepository.findById(postId)).thenReturn(Optional.of(requestedPost));
            when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

            assertThatThrownBy(() ->
                    commentService.updateComment(postId, commentId, new UpdateDto("x"))
            ).isInstanceOf(PostException.class).hasMessageContaining(PostErrorCode.COMMENT_NOT_FOUND.getMessage());

            verify(sharedPostRepository).findById(postId);
            verify(commentRepository).findById(commentId);
            verifyNoInteractions(securityUtil);
        }

        @Test
        @DisplayName("실패 - 본인 아님 → COMMENT_FORBIDDEN")
        void update_forbidden() {
            long postId = 10L;
            long commentId = 200L;
            long owner = 1L;
            long me = 2L;

            SharedPost post = SharedPost.builder().id(postId).build();
            Member author = Member.builder().id(owner).build();

            Comment comment = Comment.builder()
                    .id(commentId)
                    .content("x")
                    .member(author)
                    .sharedPost(post)
                    .build();

            when(sharedPostRepository.findById(postId)).thenReturn(Optional.of(post));
            when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
            when(securityUtil.getLoginMemberId()).thenReturn(me);

            assertThatThrownBy(() ->
                    commentService.updateComment(postId, commentId, new UpdateDto("new"))
            ).isInstanceOf(PostException.class)
                    .hasMessageContaining(PostErrorCode.COMMENT_FORBIDDEN.getMessage());

            verify(sharedPostRepository).findById(postId);
            verify(commentRepository).findById(commentId);
            verify(securityUtil).getLoginMemberId();
        }
    }

    // ===== deleteComment =====
    @Nested
    @DisplayName("deleteComment")
    class DeleteComment {

        @Test
        @DisplayName("성공 - 본인 댓글 삭제")
        void delete_success() {
            long postId = 10L;
            long commentId = 200L;
            long me = 1L;

            // given
            Member member = Member.builder().id(me).build();
            SharedPost post = SharedPost.builder().id(postId).build();
            Comment comment = Comment.builder()
                    .id(commentId)
                    .content("x")
                    .member(member)
                    .sharedPost(post)
                    .build();

            when(sharedPostRepository.findById(postId)).thenReturn(Optional.of(post));
            when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
            when(securityUtil.getLoginMemberId()).thenReturn(me);

            // when
            commentService.deleteComment(postId, commentId);

            // then
            verify(sharedPostRepository).findById(postId);
            verify(commentRepository).findById(commentId);
            verify(securityUtil).getLoginMemberId();
            verify(commentRepository).delete(comment);
        }

        @Test
        @DisplayName("실패 - 게시글 없음 → POST_NOT_FOUND")
        void delete_post_not_found() {
            long postId = 10L;
            long commentId = 200L;

            when(sharedPostRepository.findById(postId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.deleteComment(postId, commentId))
                    .isInstanceOf(PostException.class)
                    .hasMessageContaining(PostErrorCode.POST_NOT_FOUND.getMessage());

            verify(sharedPostRepository).findById(postId);
            verifyNoInteractions(commentMapper, securityUtil);
        }

        @Test
        @DisplayName("실패 - 댓글 없음 → COMMENT_NOT_FOUND")
        void delete_comment_not_found() {
            long postId = 10L;
            long commentId = 200L;

            when(sharedPostRepository.findById(postId)).thenReturn(Optional.of(SharedPost.builder().id(postId).build()));
            when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.deleteComment(postId, commentId))
                    .isInstanceOf(PostException.class)
                    .hasMessageContaining(PostErrorCode.COMMENT_NOT_FOUND.getMessage());

            verify(sharedPostRepository).findById(postId);
            verify(commentRepository).findById(commentId);
            verifyNoInteractions(securityUtil);
        }

        @Test
        @DisplayName("실패 - 다른 게시글의 댓글 → COMMENT_NOT_FOUND")
        void delete_wrong_postId() {
            long postId = 10L;
            long commentId = 200L;

            SharedPost requestedPost = SharedPost.builder().id(postId).build();
            SharedPost otherPost = SharedPost.builder().id(999L).build();
            Member member = Member.builder().id(1L).build();

            Comment comment = Comment.builder()
                    .id(commentId)
                    .content("x")
                    .member(member)
                    .sharedPost(otherPost)
                    .build();

            when(sharedPostRepository.findById(postId)).thenReturn(Optional.of(requestedPost));
            when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

            assertThatThrownBy(() -> commentService.deleteComment(postId, commentId))
                    .isInstanceOf(PostException.class)
                    .hasMessageContaining(PostErrorCode.COMMENT_NOT_FOUND.getMessage());

            verify(sharedPostRepository).findById(postId);
            verify(commentRepository).findById(commentId);
            verifyNoInteractions(securityUtil);
        }

        @Test
        @DisplayName("실패 - 본인 아님 → COMMENT_FORBIDDEN")
        void delete_forbidden() {
            long postId = 10L;
            long commentId = 200L;
            long owner = 1L;
            long me = 2L;

            SharedPost post = SharedPost.builder().id(postId).build();
            Member author = Member.builder().id(owner).build();

            Comment comment = Comment.builder()
                    .id(commentId)
                    .content("x")
                    .member(author)
                    .sharedPost(post)
                    .build();

            when(sharedPostRepository.findById(postId)).thenReturn(Optional.of(post));
            when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
            when(securityUtil.getLoginMemberId()).thenReturn(me);

            assertThatThrownBy(() -> commentService.deleteComment(postId, commentId))
                    .isInstanceOf(PostException.class)
                    .hasMessageContaining(PostErrorCode.COMMENT_FORBIDDEN.getMessage());

            verify(sharedPostRepository).findById(postId);
            verify(commentRepository).findById(commentId);
            verify(securityUtil).getLoginMemberId();
            verify(commentRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("예외 케이스")
    class ExceptionCases {

        @Test
        @DisplayName("게시글이 없으면 POST_NOT_FOUND 발생")
        void post_not_found() {
            long postId = 999L;
            when(sharedPostRepository.findById(postId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.getComments(postId, 1, 10))
                    .isInstanceOf(PostException.class)
                    .hasMessageContaining(PostErrorCode.POST_NOT_FOUND.getMessage());

            verify(sharedPostRepository).findById(postId);
            verifyNoMoreInteractions(commentMapper, securityUtil);
        }
    }

    @Nested
    @DisplayName("정상 케이스")
    class SuccessCases {

        @BeforeEach
        void setUp() {
            // 공통: 게시글 존재
            when(sharedPostRepository.findById(1L)).thenReturn(Optional.of(stubPost(1L)));
        }

        @Test
        @DisplayName("기본 페이징(page=1,size=10)과 mine 매핑 검증")
        void basic_paging_and_mine_mapping() {
            long postId = 1L;
            int page = 1;
            int size = 10;
            long me = 10L;

            // 댓글 더미(한 개는 내가 쓴 댓글, 한 개는 타인 댓글)
            var now = LocalDateTime.now();

            // 댓글 엔티티 2개 (내 댓글, 타인 댓글)
            Member meMember = Member.builder().id(me).nickname("me").build();
            Member otherMember = Member.builder().id(20L).nickname("u").build();
            SharedPost post = stubPost(postId);

            Comment myComment = Comment.builder()
                    .id(101L)
                    .content("my comment")
                    .member(meMember)
                    .sharedPost(post)
                    .build();

            Comment yourComment = Comment.builder()
                    .id(102L)
                    .content("your comment")
                    .member(otherMember)
                    .sharedPost(post)
                    .build();

            Page<Comment> commentPage = new PageImpl<>(List.of(myComment, yourComment));
            when(commentRepository.findBySharedPost_IdOrderByCreatedAtAscIdAsc(eq(postId), any(Pageable.class)))
                    .thenReturn(commentPage);
            when(commentRepository.countBySharedPost_Id(postId)).thenReturn(2L);
            when(securityUtil.getLoginMemberId()).thenReturn(me);

            PageResponse<CommentListItem> pageRes = commentService.getComments(postId, page, size);

            assertThat(pageRes.getPage()).isEqualTo(1);
            assertThat(pageRes.getSize()).isEqualTo(10);
            assertThat(pageRes.getTotalElements()).isEqualTo(2L);
            assertThat(pageRes.getContent()).hasSize(2);

            // mine 플래그 검증
            assertThat(pageRes.getContent().get(0).getCommentId()).isEqualTo(101L);
            assertThat(pageRes.getContent().get(0).isMine()).isTrue();
            assertThat(pageRes.getContent().get(1).getCommentId()).isEqualTo(102L);
            assertThat(pageRes.getContent().get(1).isMine()).isFalse();

            verify(commentRepository).findBySharedPost_IdOrderByCreatedAtAscIdAsc(eq(postId), any(Pageable.class));
            verify(commentRepository).countBySharedPost_Id(postId);
            verify(securityUtil).getLoginMemberId();
        }

        @Test
        @DisplayName("page<1 이면 1로 보정, size>50 이면 50으로 보정")
        void page_and_size_sanitization() {
            long postId = 1L;
            int reqPage = 0;   // 보정 대상
            int reqSize = 100; // 보정 대상(최대 50)
            int safePage = 1;
            int safeSize = 50;

            when(sharedPostRepository.findById(postId)).thenReturn(Optional.of(stubPost(postId)));
            when(securityUtil.getLoginMemberId()).thenReturn(999L);
            when(commentRepository.findBySharedPost_IdOrderByCreatedAtAscIdAsc(eq(postId), any(Pageable.class))).thenReturn(Page.empty());
            when(commentRepository.countBySharedPost_Id(postId)).thenReturn(0L);

            PageResponse<CommentListItem> pageRes = commentService.getComments(postId, reqPage, reqSize);

            assertThat(pageRes.getPage()).isEqualTo(safePage);
            assertThat(pageRes.getSize()).isEqualTo(safeSize);
            assertThat(pageRes.getTotalElements()).isZero();
            assertThat(pageRes.getContent()).isEmpty();

            verify(commentRepository).findBySharedPost_IdOrderByCreatedAtAscIdAsc(eq(postId), any(Pageable.class));
            verify(commentRepository).countBySharedPost_Id(postId);
        }

        @Test
        @DisplayName("page가 2 이상이면 올바른 offset(Pageable의 page index 반영)")
        void offset_calculation_when_page_gt_1() {
            long postId = 1L;
            int page = 3;
            int size = 20;

            when(sharedPostRepository.findById(postId)).thenReturn(Optional.of(stubPost(postId)));
            when(commentRepository.findBySharedPost_IdOrderByCreatedAtAscIdAsc(eq(postId), any(Pageable.class))).thenReturn(Page.empty());
            when(commentRepository.countBySharedPost_Id(postId)).thenReturn(0L);
            when(securityUtil.getLoginMemberId()).thenReturn(1L);

            commentService.getComments(postId, page, size);

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(commentRepository).findBySharedPost_IdOrderByCreatedAtAscIdAsc(eq(postId), pageableCaptor.capture());
            Pageable usedPageable = pageableCaptor.getValue();

            assertThat(usedPageable.getPageNumber()).isEqualTo(page - 1); // JPA는 0-based page index
            assertThat(usedPageable.getPageSize()).isEqualTo(size);
        }
    }
}